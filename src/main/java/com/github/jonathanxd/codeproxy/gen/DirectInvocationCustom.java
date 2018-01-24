/*
 *      CodeProxy - Proxy Pattern written on top of CodeAPI! <https://github.com/JonathanxD/CodeProxy>
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
package com.github.jonathanxd.codeproxy.gen;

import com.github.jonathanxd.codeapi.CodeInstruction;
import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.base.Access;
import com.github.jonathanxd.codeapi.base.InvokeType;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.VariableDeclaration;
import com.github.jonathanxd.codeapi.common.VariableRef;
import com.github.jonathanxd.codeapi.factory.Factories;
import com.github.jonathanxd.codeapi.factory.InvocationFactory;
import com.github.jonathanxd.codeapi.factory.VariableFactory;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.type.ImplicitCodeType;
import com.github.jonathanxd.codeapi.util.conversion.ConversionsKt;
import com.github.jonathanxd.codeproxy.internals.Util;
import com.github.jonathanxd.iutils.collection.Collections3;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/**
 * Generates direct invocation to a target, may be to instance or to a class. To a class the
 * invocation is always static, to a instance, the invocation type is resolved during method
 * generation.
 */
public interface DirectInvocationCustom extends Custom {

    /**
     * Delegates invocations to static methods of {@link #getTarget() target}.
     */
    class Static implements DirectInvocationCustom {

        /**
         * Target class to statically invoke.
         */
        private final Class<?> target;
        private final Gen gen = new Gen();

        public Static(Class<?> target) {
            this.target = target;
        }

        public Class<?> getTarget() {
            return this.target;
        }

        @Override
        public List<Property> getAdditionalProperties() {
            return Collections3.listOf(
                    new Property(new VariableRef(Class.class, "target"), Literals.TYPE(this.getTarget()))
            );
        }

        @Override
        public boolean generateSpecCache(Method m) {

            try {
                Method method = Util.getMethod(this.getTarget(), m.getName(), m.getParameterTypes());

                if (Modifier.isStatic(method.getModifiers()))
                    return false;
            } catch (NoSuchMethodException ignored) {
            }

            return true;
        }

        @Override
        public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
            return Collections3.listOf(gen);
        }

        @Override
        public int hashCode() {
            return this.getTarget().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Static && ((Static) obj).getTarget().equals(this.getTarget());

        }

        class Gen implements CustomHandlerGenerator {

            @Override
            public CodeSource handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
                try {
                    Method declaredMethod = Util.getMethod(Static.this.getTarget(), target.getName(), target.getParameterTypes());

                    if (Modifier.isStatic(declaredMethod.getModifiers())) {

                        env.setMayProceed(false);
                        env.setInvokeHandler(false);

                        return CodeSource.fromPart(Factories.returnValue(target.getReturnType(),
                                ConversionsKt.toInvocation(declaredMethod,
                                        InvokeType.INVOKE_STATIC,
                                        Access.STATIC,
                                        ConversionsKt.getAccess(methodDeclaration.getParameters())
                                )
                        ));
                    }
                } catch (NoSuchMethodException ignored) {

                }

                return CodeSource.empty();
            }
        }
    }

    /**
     * Delegates all invocations to {@link #getTarget() target instance}.
     */
    class Instance implements DirectInvocationCustom {

        /**
         * Target class to statically invoke.
         */
        private final Object target;
        private final Gen gen = new Gen();

        public Instance(Object target) {
            this.target = target;
        }

        public Object getTarget() {
            return this.target;
        }

        @Override
        public List<Property> getAdditionalProperties() {
            return Collections3.listOf(
                    new Property(new VariableRef(this.getTarget().getClass(), "target"), null)
            );
        }

        @Override
        public List<Object> getValueForConstructorProperties() {
            return Collections3.listOf(
                    this.getTarget()
            );
        }

        @Override
        public boolean generateSpecCache(Method m) {

            try {
                Util.getMethod(this.getTarget().getClass(), m.getName(), m.getParameterTypes());
                return false;
            } catch (NoSuchMethodException ignored) {
            }

            return true;
        }

        @Override
        public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
            return Collections3.listOf(gen);
        }

        @Override
        public int hashCode() {
            return this.getTarget().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Instance
                    && ((Instance) obj).getTarget().equals(this.getTarget());

        }

        class Gen implements CustomHandlerGenerator {

            @Override
            public CodeSource handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
                try {
                    Method method = Util.getMethod(Instance.this.getTarget().getClass(),
                            target.getName(), target.getParameterTypes());

                    VariableRef fprop1 = Instance.this.getAdditionalProperties().get(0).getSpec();

                    env.setMayProceed(false);
                    env.setInvokeHandler(false);

                    Type type = fprop1.getType();
                    InvokeType invokeType;

                    if (Modifier.isStatic(method.getModifiers())) {
                        invokeType = InvokeType.INVOKE_STATIC;
                    } else if (method.getDeclaringClass().isInterface()) {
                        invokeType = InvokeType.INVOKE_INTERFACE;
                        type = method.getDeclaringClass();
                    } else if (Modifier.isPrivate(method.getModifiers())) {
                        invokeType = InvokeType.INVOKE_SPECIAL;
                        type = method.getDeclaringClass();
                    } else {
                        invokeType = InvokeType.INVOKE_VIRTUAL;
                    }

                    return CodeSource.fromPart(Factories.returnValue(target.getReturnType(),
                            InvocationFactory.invoke(invokeType,
                                    type,
                                    invokeType.isStatic() ? Access.STATIC
                                            : Factories.accessThisField(fprop1.getType(),
                                            Util.getAdditionalPropertyFieldName(fprop1)),
                                    method.getName(),
                                    ConversionsKt.getTypeSpec(method),
                                    ConversionsKt.getAccess(methodDeclaration.getParameters())
                            )
                    ));


                } catch (NoSuchMethodException ignored) {

                }

                return CodeSource.empty();
            }
        }
    }

    /**
     * Delegates to a {@code target} of {@link #getTargets() targets} resolved by {@link
     * #getTargetResolver() target resolver}.
     */
    class MultiInstanceResolved implements DirectInvocationCustom {

        /**
         * Instances to invoke.
         */
        private final List<Object> targets;

        /**
         * Resolver of the position of the target instance to invoke based on a method.
         */
        private final ToIntFunction<Method> targetResolver;

        /**
         * Resolver of the base type of a instance to invoke.
         */
        private final IntFunction<Class<?>> typeResolver;
        private final Gen gen = new Gen();

        public MultiInstanceResolved(List<Object> targets,
                                     ToIntFunction<Method> targetResolver,
                                     IntFunction<Class<?>> typeResolver) {
            this.targets = Collections.unmodifiableList(new ArrayList<>(targets));
            this.targetResolver = targetResolver;
            this.typeResolver = typeResolver;
        }

        public List<Object> getTargets() {
            return this.targets;
        }

        public ToIntFunction<Method> getTargetResolver() {
            return this.targetResolver;
        }

        public IntFunction<Class<?>> getTypeResolver() {
            return this.typeResolver;
        }

        @Override
        public List<Property> getAdditionalProperties() {
            return Collections3.listOf(
                    new Property(new VariableRef(List.class, "targets"), null),
                    new Property(new VariableRef(ToIntFunction.class, "targetResolver"), null),
                    new Property(new VariableRef(IntFunction.class, "typeResolver"), null)
            );
        }

        @Override
        public List<Object> getValueForConstructorProperties() {
            return Collections3.listOf(
                    this.getTargets(),
                    this.getTargetResolver(),
                    this.getTypeResolver()
            );
        }

        @Override
        public boolean generateSpecCache(Method m) {

            try {
                int target = this.getTargetResolver().applyAsInt(m);

                List<Object> targets = this.getTargets();

                if (target > -1 && target < targets.size()) {
                    Class<?> typeCl = MultiInstanceResolved.this.getTypeResolver().apply(target);

                    Util.getMethod(typeCl, m.getName(), m.getParameterTypes());
                    return false;
                } else {
                    return true;
                }
            } catch (NoSuchMethodException ignored) {
            }

            return true;
        }

        @Override
        public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
            return Collections3.listOf(gen);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getTargets(), this.getTargetResolver(), this.getTypeResolver());
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof MultiInstanceResolved
                    && ((MultiInstanceResolved) obj).getTargets().equals(this.getTargets())
                    && ((MultiInstanceResolved) obj).getTargetResolver().equals(this.getTargetResolver())
                    && ((MultiInstanceResolved) obj).getTypeResolver().equals(this.getTypeResolver());

        }

        class Gen implements CustomHandlerGenerator {

            @Override
            public CodeSource handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
                try {
                    List<Object> targets = MultiInstanceResolved.this.getTargets();
                    ToIntFunction<Method> targetResolver = MultiInstanceResolved.this.getTargetResolver();

                    int i = targetResolver.applyAsInt(target);

                    if (i < 0 || i > targets.size()) {
                        return CodeSource.empty();
                    }

                    Class<?> typeCl = MultiInstanceResolved.this.getTypeResolver().apply(i);

                    Object targetObj = targets.get(i);

                    Method method = Util.getMethod(targetObj.getClass(),
                            target.getName(), target.getParameterTypes());

                    VariableRef fprop1 = MultiInstanceResolved.this.getAdditionalProperties().get(0).getSpec();

                    CodeInstruction access = InvocationFactory.invokeInterface(List.class,
                            Factories.accessThisField(fprop1.getType(), Util.getAdditionalPropertyFieldName(fprop1)),
                            "get",
                            Factories.typeSpec(Object.class, Integer.TYPE),
                            Collections.singletonList(Literals.INT(i)));

                    VariableDeclaration varDec = VariableFactory.variable(Object.class, "target$f", access);

                    env.setMayProceed(false);
                    env.setInvokeHandler(false);

                    Type type = typeCl; //targetObj.getClass();//fprop1.getType();
                    InvokeType invokeType;

                    if (!targetObj.getClass().isSynthetic())
                        type = targetObj.getClass();

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

                    if (ImplicitCodeType.isInterface(type) && invokeType != InvokeType.INVOKE_STATIC) {
                        invokeType = InvokeType.INVOKE_INTERFACE;
                    }

                    return CodeSource.fromVarArgs(
                            varDec,
                            Factories.returnValue(target.getReturnType(),
                                    InvocationFactory.invoke(invokeType,
                                            type,
                                            invokeType.isStatic() ? Access.STATIC
                                                    : Factories.accessVariable(varDec),
                                            method.getName(),
                                            ConversionsKt.getTypeSpec(method),
                                            ConversionsKt.getAccess(methodDeclaration.getParameters())
                                    )
                            ));


                } catch (NoSuchMethodException ignored) {

                }

                return CodeSource.empty();
            }
        }
    }

}
