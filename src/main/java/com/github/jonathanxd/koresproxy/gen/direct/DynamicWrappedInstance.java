/*
 *      KoresProxy - Proxy Pattern written on top of Kores! <https://github.com/JonathanxD/KoresProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2019 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
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
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.common.DynamicMethodSpec;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.DynamicInvocationFactory;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresproxy.bootstrap.ProxyBootstrap;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.gen.GenEnv;
import com.github.jonathanxd.koresproxy.internals.Util;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.function.Predicates;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import kotlin.collections.CollectionsKt;

public abstract class DynamicWrappedInstance extends SimpleWrappedInstance {

    /**
     * Predicate that returns whether {@link Method} should be delegated to resolved instance or
     * not.
     */
    private final Predicate<Method> delegatePredicate;

    private final Gen gen = new Gen();

    /**
     * Creates dynamic resolved instance class direct invocation.
     *
     * @param delegatePredicate Predicate that returns whether {@link Method} should be delegated to
     *                          delegated instance or not.
     */
    public DynamicWrappedInstance(Predicate<Method> delegatePredicate) {
        this.delegatePredicate = delegatePredicate;
    }

    /**
     * Creates dynamic resolved instance class direct invocation.
     *
     * Delegates all methods.
     */
    public DynamicWrappedInstance() {
        this(Predicates.acceptAll());
    }

    /**
     * Gets the predicate that test whether {@link Method} should be delegated to resolved instance
     * or not.
     *
     * @return Predicate that test whether {@link Method} should be delegated to resolved instance
     * or not.
     */
    private Predicate<Method> getDelegatePredicate() {
        return this.delegatePredicate;
    }

    @Override
    public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
        return Collections.singletonList(this.gen);
    }

    @Override
    public boolean generateSpecCache(Method m) {
        return !this.getDelegatePredicate().test(m);
    }

    class Gen implements CustomHandlerGenerator {

        @NotNull
        @Override
        public Instructions handle(@NotNull Method target, @NotNull MethodDeclaration methodDeclaration, @NotNull GenEnv env) {

            if (Modifier.isStatic(target.getModifiers())
                    || !DynamicWrappedInstance.this.getDelegatePredicate().test(target)) {
                return Instructions.empty();
            }

            VariableRef fprop1 = DynamicWrappedInstance.this.getAdditionalProperties().get(0).getSpec();

            env.setMayProceed(false);
            env.setInvokeHandler(false);

            Instruction evaluate = DynamicWrappedInstance.this.evaluate(Factories.accessThisField(fprop1.getType(),
                    Util.getAdditionalPropertyFieldName(fprop1)));

            return Instructions.fromPart(Factories.returnValue(target.getReturnType(), DynamicInvocationFactory.invokeDynamic(
                    ProxyBootstrap.BOOTSTRAP_IVK_SPEC,
                    new DynamicMethodSpec(
                            target.getName(),
                            Factories.typeSpec(target.getReturnType(),
                                    CollectionsKt.plus(Collections3.listOf(Object.class), target.getParameterTypes())),
                            CollectionsKt.plus(Collections3.listOf(evaluate),
                                    ConversionsKt.getAccess(methodDeclaration.getParameters()))
                    ),
                    Collections.emptyList()
            )));
        }
    }
}