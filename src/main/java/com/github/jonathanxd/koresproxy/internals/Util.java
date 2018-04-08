/*
 *      KoresProxy - Proxy Pattern written on top of Kores! <https://github.com/JonathanxD/KoresProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.koresproxy.internals;

import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.KoresPartKt;
import com.github.jonathanxd.kores.MutableInstructions;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.Access;
import com.github.jonathanxd.kores.base.ArrayConstructor;
import com.github.jonathanxd.kores.base.KoresModifier;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.MethodInvocation;
import com.github.jonathanxd.kores.base.TypeDeclaration;
import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.kores.bytecode.BytecodeClass;
import com.github.jonathanxd.kores.bytecode.classloader.CodeClassLoader;
import com.github.jonathanxd.kores.common.FieldRef;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.factory.PartFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.KoresType;
import com.github.jonathanxd.kores.type.Generic;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresproxy.Debug;
import com.github.jonathanxd.koresproxy.ProxyData;
import com.github.jonathanxd.koresproxy.gen.Custom;
import com.github.jonathanxd.koresproxy.gen.CustomGen;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.info.MethodInfo;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.exception.RethrowException;
import com.github.jonathanxd.iutils.object.Either;
import com.github.jonathanxd.iutils.object.Try;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.collections.CollectionsKt;

public class Util {

    /**
     * {@code List<Class<? extends CustomHandlerGenerator>>}
     */
    static KoresType LIST_OF_CUSTOM_HANDLER_GENERATORS = Generic.type(List.class).of(
            Generic.type(Class.class).of(Generic.wildcard().extends$(CustomHandlerGenerator.class)));

    /**
     * {@code List<Class<? extends CustomGen>>}
     */
    static KoresType LIST_OF_CUSTOM_GENERATORS = Generic.type(List.class).of(
            Generic.type(Class.class).of(Generic.wildcard().extends$(CustomGen.class)));

    /**
     * {@code List<? extends Custom>}
     */
    static KoresType LIST_OF_CUSTOMS = Generic.type(List.class).of(
            Generic.wildcard().extends$(Custom.class));

    static Instruction methodToReflectInvocation(Method m, FieldRef lookupFieldRef) {
        return InvocationFactory.invokeConstructor(MethodInfo.class,
                MethodInfo.CONSTRUCTOR_SPEC, // Lookup, Class, String, Class, Class[]
                Collections3.listOf(
                        PartFactory.fieldAccess().base(lookupFieldRef).build(),
                        Literals.CLASS(m.getDeclaringClass()),
                        Literals.STRING(m.getName()),
                        Literals.CLASS(m.getReturnType()),
                        Factories.createArray(
                                Class[].class,
                                Collections.singletonList(Literals.INT(m.getParameterCount())),
                                Arrays.stream(m.getParameterTypes()).map(Literals::CLASS).collect(Collectors.toList())
                        ),
                        Literals.BOOLEAN(m.isDefault())
                )
        );
    }

    static MethodDeclaration fromMethod(Method m) {
        return MethodDeclaration.Builder.builder()
                .name(m.getName())
                .modifiers(KoresModifier.PUBLIC)
                .parameters(ConversionsKt.getKoresParameters(Arrays.asList(m.getParameters())))
                .returnType(m.getReturnType())
                .body(MutableInstructions.create())
                .build();
    }

    static List<Instruction> cast(List<Instruction> list, KoresType target) {
        return list.stream().map(argument -> {
            Type type = KoresPartKt.getType(argument);

            if (ImplicitKoresType.isArray(type))
                return argument;

            if (ImplicitKoresType.compareTo(type, target) != 0) {
                return Factories.cast(type, target, argument);
            }

            return argument;
        }).collect(Collectors.toList());
    }

    public static String getAdditionalPropertyFieldName(VariableRef ref) {
        return "additional$" + ref.getName();
    }

    public static Method getMethod(Class<?> c, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return c.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            try {
                return c.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException e2) {
                e.addSuppressed(e2);
                throw e;
            }
        }
    }

    static Instruction constructProxyData(ProxyData proxyData,
                                              Type ihType,
                                              String ihName,
                                              Type csType,
                                              String csName) {

        List<? extends Instruction> arguments =
                Arrays.stream(proxyData.getInterfaces()).map(Literals::CLASS).collect(Collectors.toList());

        ArrayConstructor arrayConstructor = Factories.createArray(
                Types.CLASS.toArray(1),
                Collections.singletonList(Literals.INT(proxyData.getInterfaces().length)),
                arguments);

        return InvocationFactory.invokeConstructor(ProxyData.class,
                Factories.constructorTypeSpec(ClassLoader.class, Types.CLASS.toArray(1), Types.CLASS, ihType,
                        LIST_OF_CUSTOM_HANDLER_GENERATORS, LIST_OF_CUSTOM_GENERATORS,
                        LIST_OF_CUSTOMS),
                Collections3.listOf(
                        Util.getClassLoader_(),
                        arrayConstructor,
                        Literals.CLASS(proxyData.getSuperClass()),
                        Factories.accessThisField(ihType, ihName),
                        Util.callListOf(CollectionsKt.map(proxyData.getCustomHandlerGeneratorsView(), Literals::CLASS)),
                        Util.callListOf(CollectionsKt.map(proxyData.getCustomGeneratorsView(), Literals::CLASS)),
                        Util.callUnmod(Factories.accessThisField(csType, csName))
                )
        );
    }

    private static Instruction callListOf(List<? extends Instruction> lst) {
        return InvocationFactory.invokeStatic(Collections3.class,
                "listOf",
                new TypeSpec(List.class, Collections3.listOf(Generic.type("E").toArray(1))),
                Collections3.listOf(
                        Factories.createArray(Generic.type("E").toArray(1),
                                Collections3.listOf(Literals.INT(lst.size())),
                                lst)
                ));
    }

    private static Instruction callUnmod(Instruction insn) {
        return InvocationFactory.invokeStatic(Collections.class,
                "unmodifiableList",
                new TypeSpec(List.class, Collections3.listOf(List.class)),
                Collections3.listOf(insn));
    }

    private static MethodInvocation getClassLoader_() {
        return InvocationFactory.invokeVirtual(Class.class,
                Util.getClass_(), "getClassLoader", new TypeSpec(ClassLoader.class), Collections.emptyList());
    }

    private static MethodInvocation getClass_() {
        return InvocationFactory.invokeVirtual(
                Object.class,
                Access.THIS,
                "getClass",
                new TypeSpec(Class.class),
                Collections.emptyList()
        );
    }

    public static boolean isJava9OrSuperior() {
        Either<Exception, Integer> tryVersion =
                Try.TryEx(() -> Integer.parseInt(System.getProperty("java.version")));

        return tryVersion.isRight() && tryVersion.getRight() > 9
                || Try.TryEx(() -> Class.forName("java.lang.Module")).isRight();

    }

    public static boolean useModulesRules() {
        return !Debug.isIgnoreJavaModuleRules() && Util.isJava9OrSuperior();
    }

    static Class<?> tryLoad(ClassLoader classLoader, BytecodeClass bytecodeClass) {
        if (!(bytecodeClass.getDeclaration() instanceof TypeDeclaration))
            throw new IllegalArgumentException("Non-TypeDeclaration loading is not supported yet. BytecodeClass: " + bytecodeClass);

        TypeDeclaration decl = (TypeDeclaration) bytecodeClass.getDeclaration();
        String type = decl.getType();
        byte[] bytes = bytecodeClass.getBytecode();

        Class<?> aClass = null;
        if (!Util.useModulesRules())
            aClass = Util.tryInjectIntoClassLoaderPrivate(classLoader, type, bytes);

        if (aClass == null)
            aClass = Util.tryInjectIntoClassLoaderPublic(classLoader, type, bytes);

        if (aClass == null)
            return Util.defineWithNew(classLoader, bytecodeClass);

        return aClass;
    }

    private static Class<?> tryInjectIntoClassLoaderPublic(ClassLoader classLoader, String name, byte[] bytes) {
        try {
            Method defineClass = classLoader.getClass()
                    .getMethod("defineClass", String.class, byte[].class, int.class, int.class);

            return (Class<?>) defineClass.invoke(classLoader, name, bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private static Class<?> tryInjectIntoClassLoaderPrivate(ClassLoader classLoader, String name, byte[] bytes) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);

            defineClass.setAccessible(true);

            return (Class<?>) defineClass.invoke(classLoader, name, bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private static Class<?> defineWithNew(ClassLoader parent, BytecodeClass bytecodeClass) {
        CodeClassLoader cl = new CodeClassLoader(parent);
        return cl.define(bytecodeClass);
    }

    private static boolean isEqual(Method o1, Method o2) {
        return o1.getName().equals(o2.getName())
                && o1.getReturnType().equals(o2.getReturnType())
                && Arrays.equals(o1.getParameterTypes(), o2.getParameterTypes());
    }

    static boolean contains(Collection<Method> methods, Method o1) {
        for (Method method : methods) {
            if (method != o1 && Util.isEqual(method, o1))
                return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> cl) {
        try {
            return (T) cl.getDeclaredField("INSTANCE").get(null);
        } catch (Exception e) {
            try {
                return cl.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e1) {
                e1.addSuppressed(e);
                throw RethrowException.rethrow(e1);
            }
        }
    }

    public static boolean isHashCode(Method m) {
        return m.getName().equals("hashCode") && m.getParameterCount() == 0 && m.getReturnType().equals(Integer.TYPE);
    }

    public static boolean isToString(Method m) {
        return m.getName().equals("toString") && m.getParameterCount() == 0 && m.getReturnType().equals(String.class);
    }

    public static boolean isEquals(Method m) {
        return m.getName().equals("equals") && m.getParameterCount() == 1 && m.getReturnType().equals(Boolean.TYPE);
    }
}
