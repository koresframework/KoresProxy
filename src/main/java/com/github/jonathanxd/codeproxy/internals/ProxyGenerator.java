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

import com.github.jonathanxd.codeapi.CodeInstruction;
import com.github.jonathanxd.codeapi.MutableCodeSource;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.Access;
import com.github.jonathanxd.codeapi.base.ArrayConstructor;
import com.github.jonathanxd.codeapi.base.ClassDeclaration;
import com.github.jonathanxd.codeapi.base.CodeModifier;
import com.github.jonathanxd.codeapi.base.CodeParameter;
import com.github.jonathanxd.codeapi.base.ConstructorDeclaration;
import com.github.jonathanxd.codeapi.base.FieldDeclaration;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.TypeDeclaration;
import com.github.jonathanxd.codeapi.base.TypeSpec;
import com.github.jonathanxd.codeapi.base.VariableDeclaration;
import com.github.jonathanxd.codeapi.bytecode.BytecodeClass;
import com.github.jonathanxd.codeapi.bytecode.BytecodeOptions;
import com.github.jonathanxd.codeapi.bytecode.VisitLineType;
import com.github.jonathanxd.codeapi.bytecode.processor.BytecodeGenerator;
import com.github.jonathanxd.codeapi.common.FieldRef;
import com.github.jonathanxd.codeapi.factory.Factories;
import com.github.jonathanxd.codeapi.factory.InvocationFactory;
import com.github.jonathanxd.codeapi.factory.PartFactory;
import com.github.jonathanxd.codeapi.factory.VariableFactory;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.util.Alias;
import com.github.jonathanxd.codeapi.util.ImplicitCodeType;
import com.github.jonathanxd.codeapi.util.conversion.ConversionsKt;
import com.github.jonathanxd.codeproxy.InvokeSuper;
import com.github.jonathanxd.codeproxy.ProxyData;
import com.github.jonathanxd.codeproxy.handler.InvocationHandler;
import com.github.jonathanxd.codeproxy.info.MethodInfo;
import com.github.jonathanxd.iutils.array.ArrayUtils;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.map.WeakValueHashMap;
import com.github.jonathanxd.iutils.object.Pair;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
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
import java.util.stream.Collectors;

/**
 * Generator of proxy classes, generate classes may also include {@code package-private} method
 * handling, this is only possible when proxy generate class is defined in the same package as
 * target class.
 *
 * Generated proxy classes have 2 {@code common fields}, which holds {@link ProxyData} and {@link
 * InvocationHandler} and a {@link MethodHandles.Lookup lookup field} used to lookup and invoke
 * other methods from {@link InvocationHandler} context.
 *
 * Generated proxy class have also a {@code method table}, this is not a true table, is only a
 * mapping of method to {@link MethodInfo}. Each method has it own {@link MethodInfo}, which is a
 * constant and contains method details, the {@link MethodInfo} is provided to {@link
 * InvocationHandler}.
 */
public class ProxyGenerator {

    private static final String PD_NAME = "$ProxyData$CodeProxy";
    private static final Type PD_TYPE = ProxyData.class;

    private static final String IH_NAME = "$InvocationHandler$CodeProxy";
    private static final Type IH_TYPE = InvocationHandler.class;

    private static final Map<ProxyData, Class<?>> CACHE = new WeakValueHashMap<>();

    private static final boolean SAVE_PROXIES;
    private static long PROXY_COUNT = 0;

    static {
        Object property = System.getProperties().getProperty("codeproxy.saveproxies");
        SAVE_PROXIES = property != null && property.equals("true");
    }

    /**
     * Returns true if object {@code o} is a CodeProxy generated proxy.
     */
    public static boolean isProxy(Object o) {
        Objects.requireNonNull(o, "Argument 'o' cannot be null!");

        return o.getClass().isAnnotationPresent(Proxy.class);
    }


    /**
     * Returns true if class of proxy object {@code o} is cached internally.
     */
    public static boolean isCachedProxy(Object o) {
        Objects.requireNonNull(o, "Argument 'o' cannot be null!");

        return ProxyGenerator.CACHE.containsValue(o.getClass());
    }

    /**
     * Returns the invocation handler of the proxy {@code o}. This method uses reflection to fetch
     * the invocation handler from {@code common fields}.
     */
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


    /**
     * Returns the proxy data of the proxy {@code o}. This method uses reflection to fetch the proxy
     * data from {@code common fields}.
     */
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

    /**
     * Creates the proxy instance from proxy data.
     *
     * @param argTypes Types of argument of constructor to invoke to construct the proxy instance.
     * @param args     Arguments to pass to constructor.
     */
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

    /**
     * Constructs proxy class from proxy data.
     */
    private static Class<?> construct(ProxyData proxyData) {

        if (ProxyGenerator.CACHE.containsKey(proxyData))
            return ProxyGenerator.CACHE.get(proxyData);

        Type superType = proxyData.getSuperClass();
        List<Type> interfaces = Arrays.asList(proxyData.getInterfaces());

        boolean packagePrivate = false;
        String package_;

        if (ImplicitCodeType.compareTo(superType, Types.OBJECT) == 0
                || ImplicitCodeType.getPackageName(superType).startsWith("java.")) {
            package_ = "com.github.jonathanxd.codeproxy.generated";
        } else {
            // Probably will not work in Java 9 (or 10+ if accessible by default purpose is accepted)
            package_ = ImplicitCodeType.getPackageName(superType);
            packagePrivate = true;
        }

        ClassDeclaration.Builder proxyClassBuilder = ClassDeclaration.Builder.builder()
                .modifiers(CodeModifier.PUBLIC, CodeModifier.SYNTHETIC)
                .annotations(Factories.visibleAnnotation(Proxy.class))
                .qualifiedName(package_ + "." + ProxyGenerator.getProxyName())
                .superClass(superType)
                .implementations(interfaces);

        List<FieldDeclaration> fields = new ArrayList<>();
        List<ConstructorDeclaration> constructors = new ArrayList<>();
        List<MethodDeclaration> methods = new ArrayList<>();

        fields.addAll(ProxyGenerator.generateProxyCommonFields());

        constructors.addAll(ProxyGenerator.generateConstructor(packagePrivate, proxyData));

        Pair<List<FieldDeclaration>, List<MethodDeclaration>> listListPair =
                ProxyGenerator.generateMethods(packagePrivate, proxyData);

        fields.addAll(listListPair.getFirst());
        methods.addAll(listListPair.getSecond());

        proxyClassBuilder = proxyClassBuilder.fields(fields).constructors(constructors).methods(methods);

        ClassDeclaration proxyClass = proxyClassBuilder.build();

        BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();

        bytecodeGenerator.getOptions().set(BytecodeOptions.VISIT_LINES, VisitLineType.FOLLOW_CODE_SOURCE);

        List<? extends BytecodeClass> gen = bytecodeGenerator.process(proxyClass);

        byte[] bytes = gen.get(0).getBytecode();

        for (BytecodeClass bytecodeClass : gen) {
            ProxyGenerator.saveProxy(bytecodeClass);
        }

        Class<?> aClass = Util.injectIntoClassLoader(proxyData.getClassLoader(), proxyClass.getCanonicalName(), bytes);

        ProxyGenerator.CACHE.put(proxyData, aClass);

        return aClass;
    }

    /**
     * Generates a list with {@code common fields}.
     */
    private static List<FieldDeclaration> generateProxyCommonFields() {
        return Collections3.listOf(
                PartFactory.fieldDec().modifiers(CodeModifier.PRIVATE, CodeModifier.FINAL).type(IH_TYPE).name(IH_NAME).build(),
                PartFactory.fieldDec().modifiers(CodeModifier.PRIVATE, CodeModifier.FINAL).type(PD_TYPE).name(PD_NAME).build()
        );
    }

    /**
     * Generates constructors matching target class constructors with additional parameter at
     * position 0, which is the {@link InvocationHandler} instance.
     *
     * @see ProxyGenerator
     */
    private static List<ConstructorDeclaration> generateConstructor(boolean packagePrivate, ProxyData proxyData) {
        List<ConstructorDeclaration> constructors = new ArrayList<>();

        Class<?> superClass = proxyData.getSuperClass();

        for (Constructor<?> constructor : superClass.getConstructors()) {

            if (Modifier.isPublic(constructor.getModifiers())
                    || Modifier.isProtected(constructor.getModifiers())
                    || (isPackagePrivate(constructor.getModifiers()) && packagePrivate)) {

                List<CodeParameter> parameters =
                        ConversionsKt.getCodeParameters(Arrays.asList(constructor.getParameters()));

                parameters.add(0, Factories.parameter(IH_TYPE, IH_NAME));

                MutableCodeSource constructorSource = MutableCodeSource.create();

                ConstructorDeclaration constructorDeclaration = ConstructorDeclaration.Builder.builder()
                        .modifiers(CodeModifier.PUBLIC)
                        .parameters(parameters)
                        .body(constructorSource)
                        .build();

                if (parameters.size() > 1) {
                    List<CodeInstruction> arguments =
                            ConversionsKt.getAccess(parameters.subList(1, parameters.size()));


                    constructorSource.add(InvocationFactory.invokeSuperConstructor(
                            superClass,
                            Factories.constructorTypeSpec(constructor.getParameterTypes()),
                            arguments
                    ));

                }

                // Define common fields value
                constructorSource.add(Factories.setThisFieldValue(IH_TYPE, IH_NAME, Factories.accessVariable(IH_TYPE, IH_NAME)));
                constructorSource.add(Factories.setThisFieldValue(PD_TYPE, PD_NAME, Util.constructProxyData(proxyData, IH_TYPE, IH_NAME)));

                constructors.add(constructorDeclaration);
            }
        }

        if (constructors.size() == 0)
            throw new IllegalArgumentException("Cannot generate proxy to super class: '" + superClass + "'! No accessible constructors.");

        return constructors;
    }

    /**
     * Generates methods which delegates calls to handler, and handle the result of invocation, if
     * the result is instance of {@link InvokeSuper}, the super method is invoked through {@code
     * invokespecial}
     *
     * @see ProxyGenerator
     */
    private static Pair<List<FieldDeclaration>, List<MethodDeclaration>>
    generateMethods(boolean packagePrivate, ProxyData proxyData) {

        List<FieldDeclaration> fields = new ArrayList<>();
        List<MethodDeclaration> methods = new ArrayList<>();

        FieldRef lookupFieldRef = new FieldRef(Alias.THIS.INSTANCE, Access.STATIC, MethodHandles.Lookup.class, "lookup");

        fields.add(PartFactory.fieldDec()
                .modifiers(CodeModifier.PRIVATE, CodeModifier.STATIC, CodeModifier.FINAL)
                .type(lookupFieldRef.getType())
                .name(lookupFieldRef.getName())
                .value(InvocationFactory.invokeStatic(MethodHandles.class,
                        "lookup",
                        new TypeSpec(MethodHandles.Lookup.class), Collections.emptyList()))
                .build());

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

        List<Method> methodList = methodSet.stream().filter(
                method -> !((!Modifier.isPublic(method.getModifiers())
                        && !Modifier.isProtected(method.getModifiers())
                        && !isPackagePrivate(method.getModifiers()))
                        || Modifier.isFinal(method.getModifiers())) && !(isPackagePrivate(method.getModifiers()) && !packagePrivate)).collect(Collectors.toList());

        for (int i = 0; i < methodList.size(); i++) {
            FieldDeclaration fieldDeclaration = FieldDeclaration.Builder.builder()
                    .modifiers(CodeModifier.PRIVATE, CodeModifier.STATIC, CodeModifier.FINAL)
                    .name("$Method$" + i)
                    .type(MethodInfo.class)
                    .value(Util.methodToReflectInvocation(methodList.get(i), lookupFieldRef))
                    .build();

            fields.add(fieldDeclaration);
        }

        for (int i = 0; i < methodList.size(); i++) {
            Method method = methodList.get(i);

            MethodDeclaration methodDeclaration = Util.fromMethod(method);

            MutableCodeSource methodSource = (MutableCodeSource) methodDeclaration.getBody();

            generateMethodBody(i, method, methodDeclaration, methodSource);

            methods.add(methodDeclaration);
        }

        return Pair.of(fields, methods);
    }

    /**
     * Generates the body of proxy method.
     *
     * @param i                 Index of the method in the {@code method table} (concept), this is
     *                          used to locate constant {@link MethodInfo}.
     * @param m                 Original method to generate proxy body.
     * @param methodDeclaration Declaration of the proxy method.
     * @param methodSource      Source of the method. Instruction will be added to this source.
     */
    private static void generateMethodBody(int i, Method m, MethodDeclaration methodDeclaration, MutableCodeSource methodSource) {

        List<CodeParameter> parameterList = methodDeclaration.getParameters();

        List<? extends CodeInstruction> castArguments =
                Util.cast(ConversionsKt.getAccess(parameterList), Types.OBJECT);

        ArrayConstructor argsArray = Factories.createArray(
                Types.OBJECT.toArray(1),
                Collections.singletonList(Literals.INT(parameterList.size())),
                castArguments);

        List<? extends CodeInstruction> arguments = Collections3.listOf(
                Access.THIS,
                Factories.accessStaticField(MethodInfo.class, "$Method$" + i),
                argsArray,
                Factories.accessThisField(PD_TYPE, PD_NAME)
        );

        CodeInstruction part = InvocationFactory.invokeInterface(
                IH_TYPE,
                Factories.accessThisField(IH_TYPE, IH_NAME),
                InvocationHandler.Info.METHOD_NAME,
                InvocationHandler.Info.SPEC,
                arguments);

        Type returnType = methodDeclaration.getReturnType();

        VariableDeclaration var = VariableFactory.variable(Types.OBJECT, "result", part);

        methodSource.add(var);

        CodeInstruction invoke = InvocationFactory.invokeSpecial(
                m.getDeclaringClass(), Access.SUPER, m.getName(), methodDeclaration.getTypeSpec(),
                methodDeclaration.getParameters().stream().map(ConversionsKt::toVariableAccess).collect(Collectors.toList())
        );


        if (m.getReturnType() != Void.TYPE) {
            invoke = Factories.setVariableValue(var.getType(), var.getName(),
                    Factories.cast(returnType, Types.OBJECT, invoke)
            );
        }


        methodSource.add(Factories.ifStatement(Factories.checkTrue(Factories.isInstanceOf(
                Factories.accessVariable(var), InvokeSuper.class
        )), PartFactory.source(invoke)));

        if (m.getReturnType() != Void.TYPE) {
            methodSource.add(Factories.returnValue(returnType, Factories.cast(Types.OBJECT, returnType, Factories.accessVariable(var))));
        } else {
            methodSource.add(Factories.returnVoid());
        }

    }

    /**
     * Saves the proxy classes to {@code gen/codeproxy/} directory.
     */
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

    /**
     * Gets next unique proxy name.
     */
    private static String getProxyName() {
        long proxy = PROXY_COUNT;

        ++PROXY_COUNT;

        return "$Proxy$CodeProxy_$" + proxy;
    }

    /**
     * Returns true if receiver contains package-private flag.
     */
    public static boolean isPackagePrivate(int modifiers) {
        return !Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers) && !Modifier.isPrivate(modifiers);
    }
}
