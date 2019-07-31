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
package com.github.jonathanxd.koresproxy;

import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.MutableInstructions;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.Access;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.VariableDeclaration;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.factory.PartFactory;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresproxy.gen.CustomGen;
import com.github.jonathanxd.koresproxy.info.MethodInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

/**
 * Invokes super method of the class through {@code invokespecial} (useful to call default methods).
 * If method cannot be invoked because there is no default implementation, an {@link
 * AbstractMethodError} will be thrown by {@code JVM}.
 *
 * Note: This generator should be manually added to {@link ProxyData} in the {@link
 * ProxyData.Builder}. Factories which does not provide a way to modify {@link ProxyData.Builder}
 * already have it added to the {@link ProxyData} (see {@link KoresProxy} source).
 *
 * @apiNote Since 2.5.5, you should use {@link MethodInfo#invokeSuper(Object, Object...)} (or any
 * variant) or {@link MethodInfo#resolveSpecial(Class, Class)} to invoke super methods from {@link
 * com.github.jonathanxd.koresproxy.handler.InvocationHandler}.
 * @see com.github.jonathanxd.koresproxy.handler.InvocationHandler#invoke(Object, MethodInfo,
 * Object[], ProxyData)
 */
public final class InvokeSuper implements CustomGen {
    public static final InvokeSuper INVOKE_SUPER = new InvokeSuper();
    public static final InvokeSuper INSTANCE = INVOKE_SUPER;

    private InvokeSuper() {
    }

    @NotNull
    @Override
    public Instructions gen(@NotNull Method target,
                            @NotNull MethodDeclaration methodDeclaration,
                            @Nullable VariableDeclaration returnVariable) {

        MutableInstructions source = MutableInstructions.create();

        if (returnVariable != null) {
            Type returnType = methodDeclaration.getReturnType();

            Instruction invoke = InvocationFactory.invokeSpecial(
                    target.getDeclaringClass(), Access.SUPER, target.getName(),
                    methodDeclaration.getTypeSpec(),
                    methodDeclaration.getParameters().stream().map(ConversionsKt::toVariableAccess)
                            .collect(Collectors.toList())
            );


            if (target.getReturnType() != Void.TYPE) {
                invoke = Factories
                        .setVariableValue(returnVariable.getType(), returnVariable.getName(),
                                Factories.cast(returnType, Types.OBJECT, invoke)
                        );
            }

            source.add(Factories.ifStatement(Factories.checkTrue(Factories.isInstanceOf(
                    Factories.accessVariable(returnVariable), InvokeSuper.class
            )), PartFactory.source(invoke)));
        }

        return source;
    }
}
