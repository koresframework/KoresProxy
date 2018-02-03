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
import com.github.jonathanxd.kores.MutableInstructions;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.Access;
import com.github.jonathanxd.kores.base.Alias;
import com.github.jonathanxd.kores.base.ArrayConstructor;
import com.github.jonathanxd.kores.base.ClassDeclaration;
import com.github.jonathanxd.kores.base.KoresModifier;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.base.ConstructorDeclaration;
import com.github.jonathanxd.kores.base.FieldAccess;
import com.github.jonathanxd.kores.base.FieldDeclaration;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.TypeDeclaration;
import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.kores.base.VariableDeclaration;
import com.github.jonathanxd.kores.bytecode.BytecodeClass;
import com.github.jonathanxd.kores.bytecode.BytecodeOptions;
import com.github.jonathanxd.kores.bytecode.VisitLineType;
import com.github.jonathanxd.kores.bytecode.processor.BytecodeGenerator;
import com.github.jonathanxd.kores.common.FieldRef;
import com.github.jonathanxd.kores.common.Nothing;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.factory.PartFactory;
import com.github.jonathanxd.kores.factory.VariableFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.KoresTypes;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresproxy.Debug;
import com.github.jonathanxd.koresproxy.InvokeSuper;
import com.github.jonathanxd.koresproxy.ProxyData;
import com.github.jonathanxd.koresproxy.gen.Custom;
import com.github.jonathanxd.koresproxy.gen.CustomGen;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.gen.GenEnv;
import com.github.jonathanxd.koresproxy.handler.InvocationHandler;
import com.github.jonathanxd.koresproxy.info.MethodInfo;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.exception.RethrowException;
import com.github.jonathanxd.iutils.map.WeakValueHashMap;
import com.github.jonathanxd.iutils.object.Pair;

import org.jetbrains.annotations.Nullable;

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
import java.util.Collection;
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

    private static final String PD_NAME = "$ProxyData$KoresProxy";
    private static final Type PD_TYPE = ProxyData.class;

    private static final String IH_NAME = "$InvocationHandler$KoresProxy";
    private static final Type IH_TYPE = InvocationHandler.class;

    private static final Map<ProxyData, Class<?>> CACHE = Collections.synchronizedMap(new WeakValueHashMap<>());

    private static long PROXY_COUNT = 0;

    /**
     * Returns true if object {@code o} is a KoresProxy generated proxy.
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
        Class<?> construct = ProxyGenerator.construct(proxyData);

        try {
            List<Class<?>> types = new ArrayList<>();
            List<Object> arguments = new ArrayList<>();

            Collections.addAll(types, argTypes);
            Collections.addAll(arguments, args);

            types.add(InvocationHandler.class);
            types.add(ProxyData.class);
            arguments.add(proxyData.getHandler());
            arguments.add(proxyData);

            for (Custom custom : proxyData.getCustomView()) {
                List<Custom.Property> collect = custom.getAdditionalProperties().stream()
                        .filter(it -> !it.getInitialize().isPresent())
                        .collect(Collectors.toList());

                List<Object> valueForConstructorProperties = custom.getValueForConstructorProperties();

                for (int i = 0; i < valueForConstructorProperties.size(); i++) {
                    if (i < collect.size()) {
                        types.add((Class) KoresTypes.getKoresType(collect.get(i).getSpec().getType())
                                .getBindedDefaultResolver().resolve().getRight());
                    }

                    arguments.add(valueForConstructorProperties.get(i));
                }
            }

            return construct.getConstructor(types.toArray(new Class[0])).newInstance((Object[]) arguments.toArray(new Object[0]));
        } catch (Exception e) {
            throw RethrowException.rethrow(e);
        }
    }

    /**
     * Constructs proxy class from proxy data.
     */
    private static Class<?> construct(ProxyData proxyData) {

        synchronized (ProxyGenerator.CACHE) {
            if (ProxyGenerator.CACHE.containsKey(proxyData))
                return ProxyGenerator.CACHE.get(proxyData);
        }

        Type superType = proxyData.getSuperClass();
        List<Type> interfaces = Arrays.asList(proxyData.getInterfaces());

        boolean packagePrivate = false;
        String package_;

        if (ImplicitKoresType.compareTo(superType, Types.OBJECT) == 0
                || ImplicitKoresType.getPackageName(superType).startsWith("java.")
                || Util.useModulesRules()) {
            package_ = "com.github.jonathanxd.koresproxy.generated";
        } else {
            package_ = ImplicitKoresType.getPackageName(superType);
            packagePrivate = true;
        }

        ClassDeclaration.Builder proxyClassBuilder = ClassDeclaration.Builder.builder()
                .modifiers(KoresModifier.PUBLIC, KoresModifier.SYNTHETIC)
                .annotations(Factories.runtimeAnnotation(Proxy.class))
                .qualifiedName(package_ + "." + ProxyGenerator.getProxyName())
                .superClass(superType)
                .implementations(interfaces);

        List<FieldDeclaration> fields = new ArrayList<>();
        List<ConstructorDeclaration> constructors = new ArrayList<>();
        List<MethodDeclaration> methods = new ArrayList<>();

        fields.addAll(ProxyGenerator.generateProxyCommonFields());
        fields.addAll(ProxyGenerator.generateFields(proxyData));

        constructors.addAll(ProxyGenerator.generateConstructor(packagePrivate, proxyData));

        Pair<List<FieldDeclaration>, List<MethodDeclaration>> listListPair =
                ProxyGenerator.generateMethods(packagePrivate, proxyData);

        fields.addAll(listListPair.getFirst());
        methods.addAll(listListPair.getSecond());

        proxyClassBuilder = proxyClassBuilder.fields(fields).constructors(constructors).methods(methods);

        ClassDeclaration proxyClass = proxyClassBuilder.build();

        BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();

        bytecodeGenerator.getOptions().set(BytecodeOptions.VISIT_LINES, VisitLineType.GEN_LINE_INSTRUCTION);

        List<? extends BytecodeClass> gen = bytecodeGenerator.process(proxyClass);

        for (BytecodeClass bytecodeClass : gen) {
            ProxyGenerator.saveProxy(bytecodeClass);
        }

        Class<?> aClass = Util.tryLoad(proxyData.getClassLoader(), gen.get(0));

        ProxyGenerator.CACHE.put(proxyData, aClass);

        return aClass;
    }

    /**
     * Generates a list with {@code common fields}.
     */
    private static List<FieldDeclaration> generateProxyCommonFields() {
        return Collections3.listOf(
                PartFactory.fieldDec().modifiers(KoresModifier.PRIVATE, KoresModifier.FINAL).type(IH_TYPE).name(IH_NAME).build(),
                PartFactory.fieldDec().modifiers(KoresModifier.PRIVATE, KoresModifier.FINAL).type(PD_TYPE).name(PD_NAME).build()
        );
    }

    /**
     * Generates fields for properties.
     */
    private static List<FieldDeclaration> generateFields(ProxyData proxyData) {
        return proxyData.getCustomView().stream()
                .map(Custom::getAdditionalProperties)
                .flatMap(Collection::stream)
                .map(variableRef -> FieldDeclaration.Builder.builder()
                        .modifiers(KoresModifier.PRIVATE, KoresModifier.FINAL)
                        .type(variableRef.getSpec().getType())
                        .name(Util.getAdditionalPropertyFieldName(variableRef.getSpec()))
                        .value(variableRef.getInitialize().orElse(Nothing.INSTANCE))
                        .build())
                .collect(Collectors.toList());
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

        Set<Constructor<?>> classConstructors =
                new HashSet<>(Collections3.<Constructor<?>>prepend(
                        Arrays.asList(superClass.getDeclaredConstructors()),
                        Arrays.asList(superClass.getConstructors())
                ));

        for (Constructor<?> constructor : classConstructors) {

            if (Modifier.isPublic(constructor.getModifiers())
                    || Modifier.isProtected(constructor.getModifiers())
                    || (isPackagePrivate(constructor.getModifiers()) && packagePrivate)) {

                List<KoresParameter> parameters =
                        ConversionsKt.getKoresParameters(Arrays.asList(constructor.getParameters()));

                final int originalParametersSize = parameters.size();

                List<VariableRef> additionalProperties = proxyData.getCustomView().stream()
                        .map(Custom::getAdditionalProperties)
                        .flatMap(Collection::stream)
                        .filter(property -> !property.getInitialize().isPresent())
                        .map(Custom.Property::getSpec)
                        .collect(Collectors.toList());

                // InvocationHandler
                parameters.add(Factories.parameter(IH_TYPE, IH_NAME));
                parameters.add(Factories.parameter(PD_TYPE, PD_NAME));

                for (VariableRef variableRef : additionalProperties) {
                    parameters.add(Factories.parameter(variableRef.getType(), Util.getAdditionalPropertyFieldName(variableRef)));
                }

                MutableInstructions constructorSource = MutableInstructions.create();

                ConstructorDeclaration constructorDeclaration = ConstructorDeclaration.Builder.builder()
                        .modifiers(KoresModifier.PUBLIC)
                        .parameters(parameters)
                        .body(constructorSource)
                        .build();

                if (parameters.size() > (1 + additionalProperties.size())) {
                    List<Instruction> arguments = // Ignore IH parameter and additional parameters.
                            ConversionsKt.getAccess(parameters.subList(0, originalParametersSize));

                    constructorSource.add(InvocationFactory.invokeSuperConstructor(
                            superClass,
                            Factories.constructorTypeSpec(constructor.getParameterTypes()),
                            arguments
                    ));

                }

                // Define common fields value
                constructorSource.add(Factories.setThisFieldValue(IH_TYPE, IH_NAME,
                        Factories.accessVariable(IH_TYPE, IH_NAME)));

                constructorSource.add(Factories.setThisFieldValue(PD_TYPE, PD_NAME,
                        Factories.accessVariable(PD_TYPE, PD_NAME)));


                for (VariableRef additionalProperty : additionalProperties) {
                    constructorSource.add(Factories.setThisFieldValue(
                            additionalProperty.getType(),
                            Util.getAdditionalPropertyFieldName(additionalProperty),
                            Factories.accessVariable(additionalProperty.getType(),
                                    Util.getAdditionalPropertyFieldName(additionalProperty))));
                }

                constructors.add(constructorDeclaration);
            }
        }

        if (constructors.size() == 0)
            throw new IllegalArgumentException("Cannot generate proxy to super class: '" + superClass + "'! No accessible zero-arg constructor.");

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
                .modifiers(KoresModifier.PRIVATE, KoresModifier.STATIC, KoresModifier.FINAL)
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

        List<FieldDeclaration> cacheList = new ArrayList<>(methodList.size());

        for (int i = 0; i < methodList.size(); i++) {

            Method m = methodList.get(i);
            boolean shouldCache = true;

            for (Custom c : proxyData.getCustomView()) {
                if (!c.generateSpecCache(m)) {
                    shouldCache = false;
                    break;
                }
            }

            FieldDeclaration fieldDeclaration = FieldDeclaration.Builder.builder()
                    .modifiers(KoresModifier.PRIVATE, KoresModifier.STATIC, KoresModifier.FINAL)
                    .name("$Method$" + i)
                    .type(MethodInfo.class)
                    .value(shouldCache ? Util.methodToReflectInvocation(m, lookupFieldRef) : Literals.NULL)
                    .build();

            if (shouldCache)
                fields.add(fieldDeclaration);

            cacheList.add(fieldDeclaration);
        }

        for (int i = 0; i < methodList.size(); i++) {
            Method method = methodList.get(i);

            MethodDeclaration methodDeclaration = Util.fromMethod(method);

            MutableInstructions methodSource = (MutableInstructions) methodDeclaration.getBody();

            generateMethodBody(proxyData, i, method, cacheList.get(i), methodDeclaration, methodSource);

            methods.add(methodDeclaration);
        }

        return Pair.of(fields, methods);
    }

    /**
     * Generates the body of proxy method. Here is where custom handler generators and custom
     * generators are called.
     *
     * @param proxyData         Proxy data.
     * @param i                 Index of the method in the {@code method table} (concept), this is
     *                          used to locate constant {@link MethodInfo}.
     * @param m                 Original method to generate proxy body.
     * @param cacheField        Field with cached specification of {@code m}.
     * @param methodDeclaration Declaration of the proxy method.
     * @param methodSource      Source of the method. Instruction will be added to this source.
     */
    private static void generateMethodBody(ProxyData proxyData,
                                           int i,
                                           Method m,
                                           FieldDeclaration cacheField,
                                           MethodDeclaration methodDeclaration,
                                           MutableInstructions methodSource) {
        FieldAccess lookupAccess = Factories.accessStaticField(MethodHandles.Lookup.class, "lookup");
        FieldAccess methodInfoAccess = Factories.accessStaticField(MethodInfo.class, "$Method$" + i);

        FieldAccess proxyDataAccess = Factories.accessThisField(PD_TYPE, PD_NAME);
        FieldAccess invocationHandlerAccess = Factories.accessThisField(IH_TYPE, IH_NAME);

        boolean shouldGenInvk = true;

        int count = 0;
        for (CustomHandlerGenerator customHandler : proxyData.getCustomHandlerGeneratorsInstances()) {
            GenEnv genEnv = new GenEnv(count,
                    proxyData,
                    m,
                    methodDeclaration,
                    lookupAccess,
                    proxyDataAccess,
                    invocationHandlerAccess,
                    methodInfoAccess,
                    cacheField) {
                @Override
                public void callCustomGenerators(@Nullable VariableDeclaration returnVariable, MutableInstructions instructions) {
                    for (Class<? extends CustomGen> customGenClass : this.getProxyData().getCustomGeneratorsView()) {
                        CustomGen customGen = Util.getInstance(customGenClass);
                        instructions.addAll(customGen.gen(this.getMethod(), this.getMethodDeclaration(), returnVariable));
                    }
                }
            };

            methodSource.addAll(customHandler.handle(m, methodDeclaration, genEnv));

            shouldGenInvk &= genEnv.isInvokeHandler();

            if (!genEnv.isMayProceed()) {
                break;
            }

            ++count;
        }

        if (shouldGenInvk) {

            List<KoresParameter> parameterList = methodDeclaration.getParameters();

            List<? extends Instruction> castArguments =
                    Util.cast(ConversionsKt.getAccess(parameterList), Types.OBJECT);

            ArrayConstructor argsArray = Factories.createArray(
                    Types.OBJECT.toArray(1),
                    Collections.singletonList(Literals.INT(parameterList.size())),
                    castArguments);

            Instruction access = methodInfoAccess;

            if (cacheField.getValue().equals(Literals.NULL))
                access = Util.methodToReflectInvocation(m, new FieldRef(
                        lookupAccess.getLocalization(),
                        lookupAccess.getTarget(),
                        lookupAccess.getType(),
                        lookupAccess.getName()));

            List<? extends Instruction> arguments = Collections3.listOf(
                    Access.THIS,
                    access,
                    argsArray,
                    proxyDataAccess
            );

            Instruction part = InvocationFactory.invokeInterface(
                    IH_TYPE,
                    invocationHandlerAccess,
                    InvocationHandler.Info.METHOD_NAME,
                    InvocationHandler.Info.SPEC,
                    arguments);

            Type returnType = methodDeclaration.getReturnType();

            boolean isVoid = ImplicitKoresType.is(m.getReturnType(), Void.TYPE);

            VariableDeclaration var = VariableFactory.variable(Types.OBJECT, "result", part);

            methodSource.add(var);

            for (Class<? extends CustomGen> customGenClass : proxyData.getCustomGeneratorsView()) {
                CustomGen customGen = Util.getInstance(customGenClass);
                methodSource.addAll(customGen.gen(m, methodDeclaration, var));
            }

            if (!isVoid) {
                methodSource.add(Factories.returnValue(returnType, Factories.cast(Types.OBJECT, returnType, Factories.accessVariable(var))));
            } else {
                methodSource.add(Factories.returnVoid());
            }
        }
    }

    /**
     * Saves the proxy classes to save directory.
     */
    private static void saveProxy(BytecodeClass bytecodeClass) {
        try {

            if (!Debug.isSaveProxies())
                return;

            TypeDeclaration typeDeclaration = (TypeDeclaration) bytecodeClass.getDeclaration();

            String canonicalName = Debug.getSaveDirectory() + typeDeclaration.getCanonicalName();

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

        return "$Proxy$KoresProxy_$" + proxy;
    }

    /**
     * Returns true if receiver contains package-private flag.
     */
    public static boolean isPackagePrivate(int modifiers) {
        return !Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers) && !Modifier.isPrivate(modifiers);
    }
}
