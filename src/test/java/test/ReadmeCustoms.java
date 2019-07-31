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
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.Operate;
import com.github.jonathanxd.kores.base.VariableAccess;
import com.github.jonathanxd.kores.base.VariableDeclaration;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.operator.Operators;
import com.github.jonathanxd.koresproxy.Debug;
import com.github.jonathanxd.koresproxy.InvokeSuper;
import com.github.jonathanxd.koresproxy.KoresProxy;
import com.github.jonathanxd.koresproxy.gen.CustomGen;
import com.github.jonathanxd.koresproxy.handler.InvocationHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class ReadmeCustoms {

    @Test
    public void readmeCustoms() {
        InvocationHandler myHandler = (instance, methodInfo, args, proxyData) -> {
            if (methodInfo.getName().equals("hash"))
                return 2;
            else
                return InvokeSuper.INSTANCE;
        };

        Data data = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(ReadmeCustomGen.class.getClassLoader())
                        .addCustomGenerator(InvokeSuper.class)
                        .addCustomGenerator(ReadmeCustomGen.class)
                        .addInterface(Data.class)
                        .invocationHandler(myHandler)
        );

        Assert.assertEquals(4, data.hash());
    }

    public interface Data {
        int hash();
    }

    public static class ReadmeCustomGen implements CustomGen {

        @NotNull
        @Override
        public Instructions gen(@NotNull Method target,
                                @NotNull MethodDeclaration methodDeclaration,
                                @Nullable VariableDeclaration returnVariable) {
            // Inserts: result = result * 2; after InvocationHandler.invoke
            if (returnVariable != null && target.getName().equals("hash")) {

                VariableAccess variableAccess = Factories.accessVariable(returnVariable);
                Cast cast = Factories.cast(returnVariable.getType(), Integer.TYPE, variableAccess);
                Operate operate = Factories.operate(cast, Operators.MULTIPLY, Literals.INT(2));
                Cast result = Factories.cast(Integer.TYPE, Object.class, operate);

                return Instructions.fromPart(
                        Factories.setVariableValue(returnVariable, result)
                );
            }

            return Instructions.empty();
        }
    }
}
