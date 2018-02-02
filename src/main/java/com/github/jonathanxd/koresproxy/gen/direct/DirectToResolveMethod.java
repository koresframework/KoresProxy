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
package com.github.jonathanxd.koresproxy.gen.direct;

import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.base.Access;
import com.github.jonathanxd.kores.base.Alias;
import com.github.jonathanxd.kores.base.IfStatement;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.VariableDeclaration;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.factory.VariableFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.util.ArgumentsKt;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.gen.DirectInvocationCustom;
import com.github.jonathanxd.koresproxy.gen.GenEnv;
import com.github.jonathanxd.koresproxy.internals.Util;
import com.github.jonathanxd.iutils.collection.Collections3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Resolves the target invocation instance for every method with support to {@link ArgsResolver} and
 * {@link InvokeValidator}.
 */
public class DirectToResolveMethod implements DirectInvocationCustom {
    /**
     * Instances to be used to invoke the method, commonly resolved by index.
     */
    @NotNull
    private final List<Object> instances;

    /**
     * Resolver of target method to invoke
     */
    @NotNull
    private final Function<Method, Target> targetResolver;

    /**
     * Resolves the type to use in invocation with instance int provided by {@link
     * #targetResolver}.
     */
    @NotNull
    private final IntFunction<Class<?>> typeResolver;

    @Nullable
    private final ArgsResolver argsResolver;

    @Nullable
    private final InvokeValidator invokeValidator;

    @NotNull
    private final Gen gen = new Gen();

    public DirectToResolveMethod(@NotNull List<Object> instances,
                                 @NotNull Function<Method, Target> targetResolver,
                                 @NotNull IntFunction<Class<?>> typeResolver) {
        this(instances, targetResolver, typeResolver, null, null);
    }

    public DirectToResolveMethod(@NotNull List<Object> instances,
                                 @NotNull Function<Method, Target> targetResolver,
                                 @NotNull IntFunction<Class<?>> typeResolver,
                                 @Nullable ArgsResolver argsResolver,
                                 @Nullable InvokeValidator invokeValidator) {
        this.instances = instances;
        this.targetResolver = targetResolver;
        this.typeResolver = typeResolver;
        this.argsResolver = argsResolver;
        this.invokeValidator = invokeValidator;
    }

    @Override
    public List<Property> getAdditionalProperties() {
        return Collections3.listOf(
                new Property(new VariableRef(List.class, "instances"), null)/*,
                new Property(new VariableRef(Function.class, "targetResolver"), null),
                new Property(new VariableRef(IntFunction.class, "typeResolver"), null),
                new Property(new VariableRef(ArgsResolver.class, "argsResolver"), null)*/
        );
    }

    @NotNull
    public List<Object> getInstances() {
        return this.instances;
    }

    @NotNull
    public Function<Method, Target> getTargetResolver() {
        return this.targetResolver;
    }

    @NotNull
    public IntFunction<Class<?>> getTypeResolver() {
        return this.typeResolver;
    }

    @Nullable
    public ArgsResolver getArgsResolver() {
        return this.argsResolver;
    }

    @Nullable
    public InvokeValidator getInvokeValidator() {
        return this.invokeValidator;
    }

    @Override
    public List<Object> getValueForConstructorProperties() {
        return Collections3.listOf(
                this.getInstances()/*,
                this.getTargetResolver(),
                this.getTypeResolver(),
                this.getArgsResolver()*/
        );
    }

    @Override
    public boolean generateSpecCache(Method m) {
        return this.getTargetResolver().apply(m).getInstance() == Target.DEFAULT_BEHAVIOR;
    }

    @Override
    public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
        return Collections.singletonList(this.gen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getInstances(),
                this.getTargetResolver(),
                this.getTypeResolver(),
                this.getArgsResolver());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DirectToResolveMethod))
            return super.equals(obj);

        return Objects.equals(this.getInstances(), ((DirectToResolveMethod) obj).getInstances())
                && Objects.equals(this.getTargetResolver(), ((DirectToResolveMethod) obj).getTargetResolver())
                && Objects.equals(this.getTypeResolver(), ((DirectToResolveMethod) obj).getTypeResolver())
                && Objects.equals(this.getArgsResolver(), ((DirectToResolveMethod) obj).getArgsResolver());
    }

    public class Gen implements CustomHandlerGenerator {

        @Override
        public Instructions handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
            List<Object> instances = DirectToResolveMethod.this.getInstances();
            Function<Method, Target> targetResolver = DirectToResolveMethod.this.getTargetResolver();
            IntFunction<Class<?>> typeResolver = DirectToResolveMethod.this.getTypeResolver();
            @Nullable ArgsResolver argsResolver = DirectToResolveMethod.this.getArgsResolver();
            @Nullable InvokeValidator invokeValidator = DirectToResolveMethod.this.getInvokeValidator();

            Target invokeTarget = targetResolver.apply(target);
            int instanceIndex = invokeTarget.getInstance();

            if (instanceIndex == Target.DEFAULT_BEHAVIOR)
                return Instructions.empty();

            if ((instanceIndex < 0 && instanceIndex != Target.SELF) || instanceIndex > instances.size()) {
                return Instructions.empty();
            }

            Method method = invokeTarget.getMethod();

            List<Type> parametersTypes = Arrays.asList(method.getParameterTypes());

            VariableRef fprop1 = DirectToResolveMethod.this.getAdditionalProperties().get(0).getSpec();

            Instruction access =
                    instanceIndex == Target.SELF ? Access.THIS
                            : Modifier.isStatic(method.getModifiers()) ? Access.STATIC
                            :
                            InvocationFactory.invokeInterface(List.class,
                                    Factories.accessThisField(fprop1.getType(), Util.getAdditionalPropertyFieldName(fprop1)),
                                    "get",
                                    Factories.typeSpec(Object.class, Integer.TYPE),
                                    Collections.singletonList(Literals.INT(instanceIndex)));

            Type type = instanceIndex == Target.SELF ? Alias.THIS.INSTANCE : typeResolver.apply(instanceIndex);

            VariableDeclaration varDec = VariableFactory.variable(type, "target$f",
                    Factories.cast(Object.class, type, access));

            env.setMayProceed(false);
            env.setInvokeHandler(false);

            InvokeType invokeType;

            if (Modifier.isStatic(method.getModifiers())) {
                invokeType = InvokeType.INVOKE_STATIC;
            } else if (method.getDeclaringClass().isInterface()) {
                invokeType = InvokeType.INVOKE_INTERFACE;
                if (!method.getDeclaringClass().isSynthetic())
                    type = method.getDeclaringClass();
            } else if (Modifier.isPrivate(method.getModifiers())) {
                invokeType = InvokeType.INVOKE_SPECIAL;
                if (!method.getDeclaringClass().isSynthetic())
                    type = method.getDeclaringClass();
            } else {
                invokeType = InvokeType.INVOKE_VIRTUAL;
            }

            if (ImplicitKoresType.isInterface(type)
                    && instanceIndex != Target.SELF
                    && invokeType != InvokeType.INVOKE_STATIC) {
                invokeType = InvokeType.INVOKE_INTERFACE;
            }

            List<Instruction> arguments = new ArrayList<>(ConversionsKt.getAccess(methodDeclaration.getParameters()));

            Instructions initial = Instructions.empty();

            if (argsResolver != null) {
                initial = argsResolver.resolve(
                        target,
                        methodDeclaration,
                        method,
                        arguments
                );
            }

            if (method.getParameterCount() != arguments.size())
                throw new IllegalArgumentException("Mismatch arguments size for method (" + method.getName() + ") resolved " +
                        "by '" + targetResolver + "'. Required arguments: " + method.getParameterCount() + "." +
                        " Found arguments: " + arguments.size() + ". Proxy method: " + target + ". Target: " + method + ".");

            Instructions source = initial.plus(Instructions.fromVarArgs(
                    varDec,
                    Factories.returnValue(target.getReturnType(),
                            Factories.cast(method.getReturnType(), target.getReturnType(),
                                    InvocationFactory.invoke(invokeType,
                                            type,
                                            invokeType.isStatic() ? Access.STATIC
                                                    : Factories.accessVariable(varDec),
                                            method.getName(),
                                            ConversionsKt.getTypeSpec(method),
                                            ArgumentsKt.createCasted(
                                                    parametersTypes,
                                                    arguments
                                            )
                                    ))
                    )));

            if (invokeValidator != null) {

                List<Instruction> ifExprs =
                        invokeValidator.generateValidation(target, methodDeclaration, method,
                                new ArrayList<>(arguments));

                IfStatement statement = IfStatement.Builder.builder()
                        .expressions(ifExprs)
                        .body(source)
                        .build();

                env.setMayProceed(false);
                env.setInvokeHandler(true);

                return Instructions.fromPart(statement);
            }

            return source;

        }
    }
}
