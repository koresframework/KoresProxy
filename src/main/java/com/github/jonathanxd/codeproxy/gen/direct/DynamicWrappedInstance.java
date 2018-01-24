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
package com.github.jonathanxd.codeproxy.gen.direct;

import com.github.jonathanxd.codeapi.CodeInstruction;
import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.common.DynamicMethodSpec;
import com.github.jonathanxd.codeapi.common.VariableRef;
import com.github.jonathanxd.codeapi.factory.DynamicInvocationFactory;
import com.github.jonathanxd.codeapi.factory.Factories;
import com.github.jonathanxd.codeapi.util.conversion.ConversionsKt;
import com.github.jonathanxd.codeproxy.bootstrap.ProxyBootstrap;
import com.github.jonathanxd.codeproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.codeproxy.gen.GenEnv;
import com.github.jonathanxd.codeproxy.internals.Util;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.function.Predicates;

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

        @Override
        public CodeSource handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {

            if (Modifier.isStatic(target.getModifiers())
                    || !DynamicWrappedInstance.this.getDelegatePredicate().test(target)) {
                return CodeSource.empty();
            }

            VariableRef fprop1 = DynamicWrappedInstance.this.getAdditionalProperties().get(0).getSpec();

            env.setMayProceed(false);
            env.setInvokeHandler(false);

            CodeInstruction evaluate = DynamicWrappedInstance.this.evaluate(Factories.accessThisField(fprop1.getType(),
                    Util.getAdditionalPropertyFieldName(fprop1)));

            return CodeSource.fromPart(Factories.returnValue(target.getReturnType(), DynamicInvocationFactory.invokeDynamic(
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