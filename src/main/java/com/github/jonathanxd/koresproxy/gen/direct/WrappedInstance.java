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
import com.github.jonathanxd.kores.KoresPartKt;
import com.github.jonathanxd.kores.base.Access;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.VariableAccess;
import com.github.jonathanxd.kores.common.Commons;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.helper.Predefined;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.gen.GenEnv;
import com.github.jonathanxd.koresproxy.internals.Util;
import com.github.jonathanxd.iutils.collection.Collections3;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

public abstract class WrappedInstance extends SimpleWrappedInstance {

    /**
     * Type of wrapped instance, will be used to resolve methods. You can also use {@link
     * DynamicLazyInstance} that uses {@code invokedynamic} with {@link
     * com.github.jonathanxd.koresproxy.bootstrap.ProxyBootstrap} to invoke the method of resolved
     * {@code target}.
     */
    private final Class<?> targetClass;
    private final Gen gen = new Gen();

    /**
     * Creates wrapped instance direct invocation.
     *
     * @param targetClass Type of the wrapped object. All methods of this type that appears in proxy
     *                    class will be overwritten with delegation.
     */
    public WrappedInstance(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Gets the target class which contains methods to delegate. All methods of proxy that are
     * present in this class will be overwritten and dispatched to wrapped instance.
     *
     * @return Target class to delegate invocations.
     */
    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    @Override
    public boolean generateSpecCache(Method m) {

        try {
            Util.getMethod(this.getTargetClass(), m.getName(), m.getParameterTypes());
            return false;
        } catch (NoSuchMethodException ignored) {
            if (Util.isEquals(m) || Util.isHashCode(m) || Util.isToString(m))
                return false;
        }

        return true;
    }

    @Override
    public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
        return Collections3.listOf(this.gen);
    }

    class Gen implements CustomHandlerGenerator {

        private Instruction access() {
            VariableRef fprop1 = WrappedInstance.this.getAdditionalProperties().get(0).getSpec();

            return WrappedInstance.this.evaluate(Factories.accessThisField(fprop1.getType(),
                    Util.getAdditionalPropertyFieldName(fprop1)));
        }

        @NotNull
        @Override
        public Instructions handle(@NotNull Method target, @NotNull MethodDeclaration methodDeclaration, @NotNull GenEnv env) {
            try {
                Method method = Util.getMethod(WrappedInstance.this.getTargetClass(),
                        target.getName(), target.getParameterTypes());

                env.setMayProceed(false);
                env.setInvokeHandler(false);

                Type type = WrappedInstance.this.getTargetClass();
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

                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        InvocationFactory.invoke(invokeType,
                                type,
                                invokeType.isStatic()
                                        ? Access.STATIC
                                        : Factories.cast(Object.class, type,
                                        this.access()),
                                method.getName(),
                                ConversionsKt.getTypeSpec(method),
                                ConversionsKt.getAccess(methodDeclaration.getParameters())
                        )
                ));


            } catch (NoSuchMethodException ignored) {
                if (Util.isEquals(target)) {
                    KoresParameter p1 = methodDeclaration.getParameters().get(0);
                    VariableAccess access = Factories.accessVariable(p1.getType(), p1.getName());
                    return Instructions.fromPart(Commons.invokeObjectsEquals(this.access(), access));
                } else if (Util.isToString(target)) {
                    return Instructions.fromPart(Commons.invokeObjectsToString(this.access()));
                } else if (Util.isHashCode(target)) {
                    return Instructions.fromPart(Commons.invokeHashCode(this.access()));
                }

            }

            return Instructions.empty();
        }
    }
}