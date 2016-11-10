/*
 *      CodeProxy - Proxy Pattern written on top of CodeAPI! <https://github.com/JonathanxD/CodeProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
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
import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.MutableCodeSource;
import com.github.jonathanxd.codeapi.Result;
import com.github.jonathanxd.codeapi.common.CodeArgument;
import com.github.jonathanxd.codeapi.common.CodeModifier;
import com.github.jonathanxd.codeapi.common.CodeParameter;
import com.github.jonathanxd.codeapi.gen.value.source.PlainSourceGenerator;
import com.github.jonathanxd.codeapi.generic.GenericSignature;
import com.github.jonathanxd.codeapi.helper.Helper;
import com.github.jonathanxd.codeapi.helper.PredefinedTypes;
import com.github.jonathanxd.codeapi.impl.CodeClass;
import com.github.jonathanxd.codeapi.impl.CodeConstructor;
import com.github.jonathanxd.codeapi.impl.CodeField;
import com.github.jonathanxd.codeapi.impl.CodeMethod;
import com.github.jonathanxd.codeapi.interfaces.ArrayConstructor;
import com.github.jonathanxd.codeapi.literals.Literals;
import com.github.jonathanxd.codeapi.types.CodeType;
import com.github.jonathanxd.codeapi.gen.visit.bytecode.BytecodeGenerator;
import com.github.jonathanxd.codeproxy.ProxyData;
import com.github.jonathanxd.codeproxy.handler.InvocationHandler;
import com.github.jonathanxd.iutils.array.ArrayUtils;
import com.github.jonathanxd.iutils.array.PrimitiveArrayConverter;
import com.github.jonathanxd.iutils.map.WeakValueHashMap;
import com.github.jonathanxd.iutils.optional.Require;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ProxyGenerator {

    private static final String PD_NAME = "$ProxyData$CodeProxy";
    private static final CodeType PD_TYPE = Helper.getJavaType(ProxyData.class);

    private static final String IH_NAME = "$InvocationHandler$CodeProxy";
    private static final CodeType IH_TYPE = Helper.getJavaType(InvocationHandler.class);

    private static final Map<ProxyData, Class<?>> CACHE = new WeakValueHashMap<>();

    private static final boolean SAVE_PROXIES;
    private static long PROXY_COUNT = 0;

    static {
        Object property = System.getProperties().getProperty("codeproxy.SAVE_PROXIES");
        SAVE_PROXIES = property != null && property.equals("true");
    }

    public static boolean isProxy(Object o) {
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

        CodeType superType = Helper.getJavaType(proxyData.getSuperClass());
        List<CodeType> interfaces = Arrays.asList(Helper.getJavaTypes(proxyData.getInterfaces()));

        MutableCodeSource source = new MutableCodeSource();

        String package_;

        if (superType.compareTo(PredefinedTypes.OBJECT) == 0) {
            package_ = "com.github.jonathanxd.codeproxy.generated";
        } else {
            package_ = superType.getPackageName();
        }

        CodeClass codeClass = new CodeClass(package_ + "." + ProxyGenerator.getProxyName(),
                Collections.singletonList(CodeModifier.PUBLIC),
                superType,
                interfaces,
                GenericSignature.empty(),
                source,
                null);

        generateFields(source, proxyData);
        generateConstructor(source, proxyData);
        generateMethods(source, proxyData);
        BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();

        byte[] bytes = bytecodeGenerator.gen(Helper.sourceOf(codeClass))[0].getBytecode();

        ProxyGenerator.saveProxy(codeClass, bytes);

        Class<?> aClass = Util.injectIntoClassLoader(proxyData.getClassLoader(), codeClass.getCanonicalName(), bytes);

        ProxyGenerator.CACHE.put(proxyData, aClass);

        return aClass;
    }

    private static void generateFields(MutableCodeSource source, ProxyData proxyData) {
        source.add(new CodeField(IH_NAME, IH_TYPE, Arrays.asList(CodeModifier.PRIVATE, CodeModifier.FINAL)));
        source.add(new CodeField(PD_NAME, PD_TYPE, Arrays.asList(CodeModifier.PRIVATE, CodeModifier.FINAL)));
    }

    private static void generateConstructor(MutableCodeSource source, ProxyData proxyData) {

        Class<?> superClass = proxyData.getSuperClass();
        CodeType superType = Helper.getJavaType(superClass);

        int count = 0;

        for (Constructor<?> constructor : superClass.getConstructors()) {

            if (Modifier.isPublic(constructor.getModifiers())
                    || Modifier.isProtected(constructor.getModifiers())
                    || isPackagePrivate(constructor.getModifiers())) {

                List<CodeParameter> codeParameters = new ArrayList<>(Util.fromParameters(constructor.getParameters()));

                codeParameters.add(0, new CodeParameter(IH_NAME, IH_TYPE));

                MutableCodeSource constructorSource = new MutableCodeSource();

                CodeConstructor codeConstructor = new CodeConstructor(
                        Collections.singletonList(CodeModifier.PUBLIC),
                        codeParameters,
                        constructorSource);

                if (codeParameters.size() > 1) {
                    CodeArgument[] arguments = Util.fromParametersToArgs(codeParameters.subList(1, codeParameters.size()).stream()).toArray(CodeArgument[]::new);

                    constructorSource.add(Helper.invokeSuperInit(superType, arguments));
                }

                // this.invocationHandler = invocationHandler;
                constructorSource.add(CodeAPI.setThisField(IH_TYPE, IH_NAME, CodeAPI.accessLocalVariable(IH_TYPE, IH_NAME)));
                constructorSource.add(CodeAPI.setThisField(PD_TYPE, PD_NAME, Util.constructProxyData(proxyData, IH_TYPE, IH_NAME)));

                source.add(codeConstructor);

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

            CodeMethod codeMethod = Util.fromMethod(method);

            MutableCodeSource methodSource = (MutableCodeSource) Require.require(codeMethod.getBody());

            generateMethod(method, codeMethod, methodSource, proxyData);

            source.add(codeMethod);
        }
    }


    private static void generateMethod(Method m, CodeMethod codeMethod, MutableCodeSource methodSource, ProxyData proxyData) {

        List<CodeParameter> parameterList = codeMethod.getParameters();

        CodeArgument[] codeArgumentArray = Util.cast(Util.fromParametersToArgs(parameterList.stream()), PredefinedTypes.OBJECT).toArray(CodeArgument[]::new);

        ArrayConstructor argsArray = CodeAPI.arrayConstruct(Object.class, new CodePart[]{Literals.INT(parameterList.size())}, codeArgumentArray);

        CodeArgument[] arguments = {
                new CodeArgument(Helper.accessThis()),
                new CodeArgument(Util.methodToReflectInvocation(m)),
                new CodeArgument(argsArray),
                new CodeArgument(CodeAPI.accessThisField(PD_TYPE, PD_NAME))
        };

        CodePart part = CodeAPI.invokeInterface(IH_TYPE,
                CodeAPI.accessThisField(IH_TYPE, IH_NAME),
                InvocationHandler.Info.METHOD_NAME,
                InvocationHandler.Info.SPEC, arguments);

        CodeType returnType = codeMethod.getReturnType().orElseThrow(NullPointerException::new);

        if (m.getReturnType() != Void.TYPE) {

            if (m.getReturnType() != Object.class) {
                part = Helper.cast(PredefinedTypes.OBJECT, returnType, part);
            }

            part = Helper.returnValue(Helper.getJavaType(m.getReturnType()), part);
        }

        methodSource.add(part);
    }

    private static void saveProxy(CodeClass codeClass, byte[] bytes) {
        try {

            if (!SAVE_PROXIES)
                return;

            PlainSourceGenerator plainSourceGenerator = new PlainSourceGenerator();
            String gen = plainSourceGenerator.gen(Helper.sourceOf(codeClass));

            String canonicalName = "gen/codeproxy/" + codeClass.getCanonicalName();

            canonicalName = canonicalName.replace('.', '/');

            File file = new File(canonicalName);

            if (file.getParentFile() != null && file.getParentFile().exists()) {
                file.getParentFile().delete();
            }

            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }

            Files.write(Paths.get(canonicalName + ".java"), gen.getBytes("UTF-8"), StandardOpenOption.CREATE);
            Files.write(Paths.get(canonicalName + ".class"), bytes, StandardOpenOption.CREATE);

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
