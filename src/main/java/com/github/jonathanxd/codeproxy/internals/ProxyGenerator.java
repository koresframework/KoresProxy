/*
 *      CodeProxy - Proxy Pattern written on top of CodeAPI! <https://github.com/JonathanxD/CodeProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2017 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
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
package com.github.jonathanxd.codeproxy.internals;

import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.MutableCodeSource;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.ArrayConstructor;
import com.github.jonathanxd.codeapi.base.ClassDeclaration;
import com.github.jonathanxd.codeapi.base.ConstructorDeclaration;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.TypeDeclaration;
import com.github.jonathanxd.codeapi.builder.ClassDeclarationBuilder;
import com.github.jonathanxd.codeapi.builder.ConstructorDeclarationBuilder;
import com.github.jonathanxd.codeapi.bytecode.BytecodeClass;
import com.github.jonathanxd.codeapi.bytecode.BytecodeOptions;
import com.github.jonathanxd.codeapi.bytecode.VisitLineType;
import com.github.jonathanxd.codeapi.bytecode.gen.BytecodeGenerator;
import com.github.jonathanxd.codeapi.common.CodeModifier;
import com.github.jonathanxd.codeapi.common.CodeParameter;
import com.github.jonathanxd.codeapi.factory.FieldFactory;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeproxy.ProxyData;
import com.github.jonathanxd.codeproxy.handler.InvocationHandler;
import com.github.jonathanxd.iutils.array.ArrayUtils;
import com.github.jonathanxd.iutils.map.WeakValueHashMap;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import kotlin.collections.CollectionsKt;

public class ProxyGenerator {

    private static final String PD_NAME = "$ProxyData$CodeProxy";
    private static final CodeType PD_TYPE = CodeAPI.getJavaType(ProxyData.class);

    private static final String IH_NAME = "$InvocationHandler$CodeProxy";
    private static final CodeType IH_TYPE = CodeAPI.getJavaType(InvocationHandler.class);

    private static final Map<ProxyData, Class<?>> CACHE = new WeakValueHashMap<>();

    private static final boolean SAVE_PROXIES;
    private static long PROXY_COUNT = 0;

    static {
        Object property = System.getProperties().getProperty("codeproxy.saveproxies");
        SAVE_PROXIES = property != null && property.equals("true");
    }

    public static boolean isProxy(Object o) {
        Objects.requireNonNull(o, "Argument 'o' cannot be null!");

        return o.getClass().isAnnotationPresent(Proxy.class);
    }

    public static boolean isCachedProxy(Object o) {
        Objects.requireNonNull(o, "Argument 'o' cannot be null!");

        return ProxyGenerator.CACHE.containsValue(o.getClass());
    }

    public static InvocationHandler getInvocationHandler(Object o) {

        Objects.requireNonNull(o, "Argument 'o' cannot be null!");

        if (!ProxyGenerator.isProxy(o))
            throw new IllegalArgumentException("Object '" + o + "' isn't a Proxy!");

        Class<?> aClass = o.getClass();

        try {
            Field declaredField = aClass.getDeclaredField(IH_NAME);

            if (!declaredField.getType().equals(InvocationHandler.class))
                throw new IllegalStateException("Illegal field type: '" + declaredField.getType() + "'!");

            declaredField.setAccessible(true);

            return (InvocationHandler) declaredField.get(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ProxyData getProxyData(Object o) {

        Objects.requireNonNull(o, "Argument 'o' cannot be null!");

        if (!ProxyGenerator.isProxy(o))
            throw new IllegalArgumentException("Object '" + o + "' isn't a Proxy!");

        Class<?> aClass = o.getClass();

        try {
            Field declaredField = aClass.getDeclaredField(PD_NAME);

            if (!declaredField.getType().equals(ProxyData.class))
                throw new IllegalStateException("Illegal field type: '" + declaredField.getType() + "'!");

            declaredField.setAccessible(true);

            return (ProxyData) declaredField.get(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object create(ProxyData proxyData, Class<?>[] argTypes, Object[] args) {
        try {
            Class<?> construct = ProxyGenerator.construct(proxyData);

            Class[] types = new Class[]{InvocationHandler.class};
            Object[] arguments = new Object[]{proxyData.getHandler()};

            if (args.length > 0) {
                types = ArrayUtils.addAllToArray(types, argTypes);
                arguments = ArrayUtils.addAllToArray(arguments, args);
            }

            return construct.getConstructor(types).newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> construct(ProxyData proxyData) {

        if (ProxyGenerator.CACHE.containsKey(proxyData))
            return ProxyGenerator.CACHE.get(proxyData);

        CodeType superType = CodeAPI.getJavaType(proxyData.getSuperClass());
        List<CodeType> interfaces = Arrays.asList(CodeAPI.getJavaTypes(proxyData.getInterfaces()));

        MutableCodeSource source = new MutableCodeSource();

        String package_;

        if (superType.compareTo(Types.OBJECT) == 0) {
            package_ = "com.github.jonathanxd.codeproxy.generated";
        } else {
            package_ = superType.getPackageName();
        }

        ClassDeclaration proxyClass = ClassDeclarationBuilder.builder()
                .withModifiers(EnumSet.of(CodeModifier.PUBLIC, CodeModifier.SYNTHETIC))
                .withAnnotations(CodeAPI.visibleAnnotation(Proxy.class))
                .withQualifiedName(package_ + "." + ProxyGenerator.getProxyName())
                .withSuperClass(superType)
                .withImplementations(interfaces)
                .withBody(source)
                .build();

        generateFields(source, proxyData);
        generateConstructor(source, proxyData);
        generateMethods(source, proxyData);

        BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();

        bytecodeGenerator.getOptions().set(BytecodeOptions.VISIT_LINES, VisitLineType.FOLLOW_CODE_SOURCE);

        BytecodeClass[] gen = bytecodeGenerator.gen(proxyClass);

        byte[] bytes = gen[0].getBytecode();

        for (BytecodeClass bytecodeClass : gen) {
            ProxyGenerator.saveProxy(bytecodeClass);
        }


        Class<?> aClass = Util.injectIntoClassLoader(proxyData.getClassLoader(), proxyClass.getCanonicalName(), bytes);

        ProxyGenerator.CACHE.put(proxyData, aClass);

        return aClass;
    }

    private static void generateFields(MutableCodeSource source, ProxyData proxyData) {
        source.add(FieldFactory.field(EnumSet.of(CodeModifier.PRIVATE, CodeModifier.FINAL), IH_TYPE, IH_NAME));
        source.add(FieldFactory.field(EnumSet.of(CodeModifier.PRIVATE, CodeModifier.FINAL), PD_TYPE, PD_NAME));
    }

    private static void generateConstructor(MutableCodeSource source, ProxyData proxyData) {

        Class<?> superClass = proxyData.getSuperClass();
        CodeType superType = CodeAPI.getJavaType(superClass);

        int count = 0;

        for (Constructor<?> constructor : superClass.getConstructors()) {

            if (Modifier.isPublic(constructor.getModifiers())
                    || Modifier.isProtected(constructor.getModifiers())
                    || isPackagePrivate(constructor.getModifiers())) {

                List<CodeParameter> parameters = new ArrayList<>(Util.fromParameters(constructor.getParameters()));

                parameters.add(0, new CodeParameter(IH_TYPE, IH_NAME));

                MutableCodeSource constructorSource = new MutableCodeSource();

                ConstructorDeclaration constructorDeclaration = ConstructorDeclarationBuilder.builder()
                        .withModifiers(CodeModifier.PUBLIC)
                        .withParameters(parameters)
                        .withBody(constructorSource)
                        .build();

                if (parameters.size() > 1) {
                    List<CodePart> arguments = Util.fromParametersToArgs(parameters.subList(1, parameters.size()).stream()).collect(Collectors.toList());


                    constructorSource.add(CodeAPI.invokeSuperConstructor(
                            superType,
                            CodeAPI.constructorTypeSpec(constructor.getParameterTypes()),
                            arguments
                    ));

                }

                // this.invocationHandler = invocationHandler;
                constructorSource.add(CodeAPI.setThisField(IH_TYPE, IH_NAME, CodeAPI.accessLocalVariable(IH_TYPE, IH_NAME)));
                constructorSource.add(CodeAPI.setThisField(PD_TYPE, PD_NAME, Util.constructProxyData(proxyData, IH_TYPE, IH_NAME)));

                source.add(constructorDeclaration);

                ++count;
            }
        }

        if (count == 0)
            throw new IllegalArgumentException("Cannot generate proxy to super class: '" + superClass + "'! No accessible constructors.");

    }

    private static void generateMethods(MutableCodeSource source, ProxyData proxyData) {
        Class<?> superClass = proxyData.getSuperClass();
        Class<?>[] interfaces = proxyData.getInterfaces();

        Set<Method> methodSet = new HashSet<>();

        Collections.addAll(methodSet, superClass.getMethods());
        Collections.addAll(methodSet, superClass.getDeclaredMethods());

        for (Class<?> anInterface : interfaces) {
            Collections.addAll(methodSet, anInterface.getMethods());
            Collections.addAll(methodSet, anInterface.getDeclaredMethods());
        }

        methodSet.removeIf(method -> Util.contains(methodSet, method));

        for (Method method : methodSet) {

            if ((!Modifier.isPublic(method.getModifiers())
                    && !Modifier.isProtected(method.getModifiers())
                    && !isPackagePrivate(method.getModifiers()))
                    || Modifier.isFinal(method.getModifiers())) {
                continue;
            }

            MethodDeclaration methodDeclaration = Util.fromMethod(method);

            MutableCodeSource methodSource = (MutableCodeSource) methodDeclaration.getBody();

            generateMethod(method, methodDeclaration, methodSource, proxyData);

            source.add(methodDeclaration);
        }
    }


    private static void generateMethod(Method m, MethodDeclaration methodDeclaration, MutableCodeSource methodSource, ProxyData proxyData) {

        List<CodeParameter> parameterList = methodDeclaration.getParameters();

        List<CodePart> codeArgumentArray = Util.cast(Util.fromParametersToArgs(parameterList.stream()), Types.OBJECT).collect(Collectors.toList());

        ArrayConstructor argsArray = CodeAPI.arrayConstruct(Types.OBJECT.toArray(1), new CodePart[]{Literals.INT(parameterList.size())}, codeArgumentArray);

        List<CodePart> arguments = CollectionsKt.listOf(
                CodeAPI.accessThis(),
                Util.methodToReflectInvocation(m),
                argsArray,
                CodeAPI.accessThisField(PD_TYPE, PD_NAME)
        );

        CodePart part = CodeAPI.invokeInterface(IH_TYPE,
                CodeAPI.accessThisField(IH_TYPE, IH_NAME),
                InvocationHandler.Info.METHOD_NAME,
                InvocationHandler.Info.SPEC,
                arguments);

        CodeType returnType = methodDeclaration.getReturnType();

        if (m.getReturnType() != Void.TYPE) {

            if (m.getReturnType() != Object.class) {
                part = CodeAPI.cast(Types.OBJECT, returnType, part);
            }

            part = CodeAPI.returnValue(CodeAPI.getJavaType(m.getReturnType()), part);
        }

        methodSource.add(part);
    }

    private static void saveProxy(BytecodeClass bytecodeClass) {
        try {

            if (!SAVE_PROXIES)
                return;

            TypeDeclaration typeDeclaration = bytecodeClass.getType();

            String canonicalName = "gen/codeproxy/" + typeDeclaration.getCanonicalName();

            canonicalName = canonicalName.replace('.', '/');

            File file = new File(canonicalName);

            if (file.getParentFile() != null && file.getParentFile().exists()) {
                file.getParentFile().delete();
            }

            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }

            Files.write(Paths.get(canonicalName + ".disassembled"), bytecodeClass.getDisassembledCode().getBytes("UTF-8"), StandardOpenOption.CREATE);
            Files.write(Paths.get(canonicalName + ".class"), bytecodeClass.getBytecode(), StandardOpenOption.CREATE);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getProxyName() {
        long proxy = PROXY_COUNT;

        ++PROXY_COUNT;

        return "$Proxy$CodeProxy_$" + proxy;
    }

    public static boolean isPackagePrivate(int modifiers) {
        return !Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers) && !Modifier.isPrivate(modifiers);
    }
}
