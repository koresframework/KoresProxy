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
package com.github.jonathanxd.codeproxy.gen;

import com.github.jonathanxd.codeapi.CodeInstruction;
import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.base.Access;
import com.github.jonathanxd.codeapi.base.InvokeType;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.common.VariableRef;
import com.github.jonathanxd.codeapi.factory.Factories;
import com.github.jonathanxd.codeapi.factory.InvocationFactory;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.util.conversion.ConversionsKt;
import com.github.jonathanxd.codeproxy.internals.Util;
import com.github.jonathanxd.iutils.collection.Collections3;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Generates direct invocation to a target, may be to instance or to a class. To a class the
 * invocation is always static, to a instance, the invocation type is resolved during method
 * generation.
 */
public interface DirectInvocationCustom extends Custom {

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
        public CodeInstruction toInstruction() {
            return InvocationFactory.invokeConstructor(Static.class,
                    Factories.constructorTypeSpec(Class.class),
                    Collections.singletonList(Literals.TYPE(this.getTarget())));
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

                return true;
            } catch (NoSuchMethodException ignored) {
            }

            return true;
        }

        @Override
        public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
            return Collections3.listOf(gen);
        }

        @Override
        public CodeInstruction toInstruction() {

            VariableRef fprop1 = this.getAdditionalProperties().get(0).getSpec();

            return InvocationFactory.invokeConstructor(Instance.class,
                    Factories.constructorTypeSpec(Object.class),
                    Collections3.listOf(
                            Factories.accessVariable(fprop1.getType(),
                                    Util.getAdditionalPropertyFieldName(fprop1))
                    )
            );
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
}
