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
package test;

import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.base.Cast;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.Operate;
import com.github.jonathanxd.kores.base.VariableAccess;
import com.github.jonathanxd.kores.base.VariableDeclaration;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.operator.Operators;
import com.github.jonathanxd.koresproxy.Debug;
import com.github.jonathanxd.koresproxy.InvokeSuper;
import com.github.jonathanxd.koresproxy.KoresProxy;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.gen.GenEnv;
import com.github.jonathanxd.koresproxy.handler.InvocationHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class ReadmeCustomHandlerGenerators {

    @Test
    public void readmeCustomHandlerGenerators() {
        InvocationHandler myHandler = (instance, methodInfo, args, proxyData) -> {
            if (methodInfo.getName().equals("calc"))
                return (Integer) args[0] + (Integer) args[1];
            else
                return InvokeSuper.INSTANCE;
        };

        Data data = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(ReadmeCustomHandlerGenerators.class.getClassLoader())
                        .addCustomGenerator(InvokeSuper.class)
                        .addCustomHandlerGenerator(ReadmeCustomHGenerator.class)
                        .addInterface(Data.class)
                        .invocationHandler(myHandler)
        );

        Assert.assertEquals(((5 * 10) + (5 * 10)), data.calc(5, 5));
    }

    public interface Data {
        int calc(int a, int b);
    }

    public static class ReadmeCustomHGenerator implements CustomHandlerGenerator {

        @NotNull
        @Override
        public Instructions handle(@NotNull Method target, @NotNull MethodDeclaration methodDeclaration, @NotNull GenEnv env) {
            if (target.getName().equals("calc")) {
                KoresParameter p1 = methodDeclaration.getParameters().get(0);
                KoresParameter p2 = methodDeclaration.getParameters().get(1);
                VariableRef v1 = new VariableRef(p1.getType(), p1.getName());
                VariableRef v2 = new VariableRef(p2.getType(), p2.getName());

                VariableAccess access1 = Factories.accessVariable(v1);
                VariableAccess access2 = Factories.accessVariable(v2);

                return Instructions.fromVarArgs(
                        Factories.setVariableValue(v1, Factories.operate(access1, Operators.MULTIPLY, Literals.INT(10))),
                        Factories.setVariableValue(v2, Factories.operate(access2, Operators.MULTIPLY, Literals.INT(10)))
                );

            }

            return Instructions.empty();
        }
    }
}
