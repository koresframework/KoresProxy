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
import com.github.jonathanxd.codeapi.base.InvokeType;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.VariableDeclaration;
import com.github.jonathanxd.codeapi.common.VariableRef;
import com.github.jonathanxd.codeapi.factory.Factories;
import com.github.jonathanxd.codeapi.factory.InvocationFactory;
import com.github.jonathanxd.codeapi.factory.VariableFactory;
import com.github.jonathanxd.codeapi.literal.Literal;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.util.conversion.ConversionsKt;
import com.github.jonathanxd.codeproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.codeproxy.gen.DirectInvocationCustom;
import com.github.jonathanxd.codeproxy.gen.GenEnv;
import com.github.jonathanxd.codeproxy.internals.Util;
import com.github.jonathanxd.iutils.collection.Collections3;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class DirectToFunction implements DirectInvocationCustom {
    public static final Object[] EMPTY_ARRAY = new Object[0];

    /**
     * Functions instance to invoke.
     */
    private final List<Function<Object[], Object>> functions;

    /**
     * Resolver of the position of the functions instance to invoke based on a method.
     */
    private final ToIntFunction<Method> functionResolver;

    private final Gen gen = new Gen();

    public DirectToFunction(List<Function<Object[], Object>> functions,
                            ToIntFunction<Method> functionResolver) {
        this.functions = functions;
        this.functionResolver = functionResolver;
    }


    @Override
    public List<Property> getAdditionalProperties() {
        return Collections3.listOf(
                new Property(new VariableRef(List.class, "functions"), null),
                new Property(new VariableRef(ToIntFunction.class, "functionResolver"), null)
        );
    }

    public List<Function<Object[], Object>> getFunctions() {
        return this.functions;
    }

    public ToIntFunction<Method> getFunctionResolver() {
        return this.functionResolver;
    }

    @Override
    public List<Object> getValueForConstructorProperties() {
        return Collections3.listOf(
                this.getFunctions(),
                this.getFunctionResolver()
        );
    }

    @Override
    public boolean generateSpecCache(Method m) {
        return functionResolver.applyAsInt(m) == -1;
    }

    @Override
    public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
        return Collections.singletonList(this.gen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getFunctions(), this.getFunctionResolver());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DirectToFunction))
            return super.equals(obj);

        return Objects.equals(this.getFunctions(), ((DirectToFunction) obj).getFunctions())
                && Objects.equals(this.getFunctionResolver(), ((DirectToFunction) obj).getFunctionResolver());
    }

    public class Gen implements CustomHandlerGenerator {

        @Override
        public CodeSource handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
            List<Function<Object[], Object>> functions = DirectToFunction.this.getFunctions();
            ToIntFunction<Method> targetResolver = DirectToFunction.this.getFunctionResolver();

            int i = targetResolver.applyAsInt(target);

            if (i < 0 || i > functions.size()) {
                return CodeSource.empty();
            }

            VariableRef fprop1 = DirectToFunction.this.getAdditionalProperties().get(0).getSpec();

            CodeInstruction access = InvocationFactory.invokeInterface(List.class,
                    Factories.accessThisField(fprop1.getType(), Util.getAdditionalPropertyFieldName(fprop1)),
                    "get",
                    Factories.typeSpec(Object.class, Integer.TYPE),
                    Collections.singletonList(Literals.INT(i)));

            VariableDeclaration varDec = VariableFactory.variable(Object.class, "target$f", access);

            env.setMayProceed(false);
            env.setInvokeHandler(false);

            List<Literal> size = Collections3.listOf(Literals.INT(methodDeclaration.getParameters().size()));

            CodeInstruction argument;

            if (methodDeclaration.getParameters().isEmpty())
                argument = Factories.accessStaticField(DirectToFunction.class,
                        Object[].class, "EMPTY_ARRAY");
            else
                argument = Factories.createArray(Object[].class,
                        size,
                        ConversionsKt.getAccess(methodDeclaration.getParameters()));

            return CodeSource.fromVarArgs(
                    varDec,
                    Factories.returnValue(target.getReturnType(),
                            Factories.cast(Object.class, target.getReturnType(),
                                    InvocationFactory.invoke(InvokeType.INVOKE_INTERFACE,
                                            Function.class,
                                            Factories.accessVariable(varDec),
                                            "apply",
                                            Factories.typeSpec(Object.class, Object.class),
                                            Collections3.listOf(argument)
                                    ))
                    ));


        }
    }
}
