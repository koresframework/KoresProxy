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
package com.github.jonathanxd.codeproxy.gen.direct;

import com.github.jonathanxd.codeapi.CodeInstruction;
import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.base.Access;
import com.github.jonathanxd.codeapi.base.InvokeType;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.common.VariableRef;
import com.github.jonathanxd.codeapi.factory.Factories;
import com.github.jonathanxd.codeapi.factory.InvocationFactory;
import com.github.jonathanxd.codeapi.util.conversion.ConversionsKt;
import com.github.jonathanxd.codeproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.codeproxy.gen.DirectInvocationCustom;
import com.github.jonathanxd.codeproxy.gen.GenEnv;
import com.github.jonathanxd.codeproxy.internals.Util;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.object.Lazy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class LazyInstance implements DirectInvocationCustom {

    /**
     * Target instance to invoke methods.
     */
    private final Lazy<?> targetLazy;

    /**
     * Type of object that will be evaluated by the {@code target} lazy, this type is used to invoke
     * the method. You can also use {@link DynamicLazyInstance} that uses {@code invokedynamic} with
     * {@link com.github.jonathanxd.codeproxy.bootstrap.ProxyBootstrap} to invoke the method of
     * resolved {@code target}.
     */
    private final Class<?> targetClass;
    private final Gen gen = new Gen();

    /**
     * Creates lazy instance direct invocation.
     *
     * @param targetLazy  Lazy instance.
     * @param targetClass Type of object that will be evaluated by {@code targetLazy}
     */
    public LazyInstance(Lazy<?> targetLazy,
                        Class<?> targetClass) {
        this.targetLazy = targetLazy;
        this.targetClass = targetClass;
    }

    private static CodeInstruction evaluate(CodeInstruction lazy) {
        return InvocationFactory.invokeVirtual(Lazy.class,
                lazy,
                "get",
                Factories.typeSpec(Object.class),
                Collections.emptyList());
    }

    public Lazy<?> getTargetLazy() {
        return this.targetLazy;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    @Override
    public List<Property> getAdditionalProperties() {
        return Collections3.listOf(
                new Property(new VariableRef(Lazy.class, "targetLazy"), null)
        );
    }

    @Override
    public List<Object> getValueForConstructorProperties() {
        return Collections3.listOf(
                this.getTargetLazy()
        );
    }

    @Override
    public boolean generateSpecCache(Method m) {

        try {
            Util.getMethod(this.getTargetClass(), m.getName(), m.getParameterTypes());
            return false;
        } catch (NoSuchMethodException ignored) {
        }

        return true;
    }

    @Override
    public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
        return Collections3.listOf(gen);
    }

    class Gen implements CustomHandlerGenerator {

        @Override
        public CodeSource handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
            try {
                Method method = Util.getMethod(LazyInstance.this.getTargetClass(),
                        target.getName(), target.getParameterTypes());

                VariableRef fprop1 = LazyInstance.this.getAdditionalProperties().get(0).getSpec();

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
                                        :
                                        LazyInstance.evaluate(Factories.accessThisField(fprop1.getType(),
                                                Util.getAdditionalPropertyFieldName(fprop1))),
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