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

import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.gen.DirectInvocationCustom;
import com.github.jonathanxd.koresproxy.gen.GenEnv;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Generates a dummy implementation, all methods returns either a default value or {@code null}.
 */
public class DummyCustom implements DirectInvocationCustom {

    private static final DummyCustom INSTANCE = new DummyCustom();
    private final DummyCustom.Gen gen = new DummyCustom.Gen();

    private DummyCustom() {
    }

    /**
     * Gets dummy custom instance.
     *
     * @return Dummy custom instance.
     */
    public static DummyCustom getInstance() {
        return DummyCustom.INSTANCE;
    }

    @Override
    public List<Property> getAdditionalProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<Object> getValueForConstructorProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<CustomHandlerGenerator> getCustomHandlerGenerators() {
        return Collections3.listOf(gen);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DummyCustom;

    }

    static class Gen implements CustomHandlerGenerator {

        @NotNull
        @Override
        public Instructions handle(@NotNull Method target, @NotNull MethodDeclaration methodDeclaration, @NotNull GenEnv env) {

            env.setMayProceed(false);
            env.setInvokeHandler(false);

            switch (target.getName()) {
                case "toString":
                    return Instructions.fromPart(Factories.returnValue(String.class, InvocationFactory.invokeSpecial(target.getDeclaringClass(),
                            Factories.accessSuper(), "toString", Factories.typeSpec(String.class), Collections.emptyList())));
                case "hashCode":
                    return Instructions.fromPart(Factories.returnValue(Integer.TYPE, InvocationFactory.invokeSpecial(target.getDeclaringClass(),
                            Factories.accessSuper(), "hashCode", Factories.typeSpec(Integer.TYPE), Collections.emptyList())));
                case "equals":
                    KoresParameter param0 = methodDeclaration.getParameters().get(0);
                    return Instructions.fromPart(Factories.returnValue(Boolean.TYPE, InvocationFactory.invokeSpecial(target.getDeclaringClass(),
                            Factories.accessSuper(), "equals", Factories.typeSpec(Boolean.TYPE, Object.class), Collections.singletonList(Factories.accessVariable(param0.getType(), param0.getName())))));
            }

            if (target.getReturnType() == Byte.TYPE) {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        Literals.BYTE((byte) 0)));
            } else if (target.getReturnType() == Short.TYPE) {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        Literals.SHORT((short) 0)));
            } else if (target.getReturnType() == Character.TYPE) {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        Literals.CHAR((char) 0)));
            } else if (target.getReturnType() == Boolean.TYPE) {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        Literals.BOOLEAN(false)));
            } else if (target.getReturnType() == Integer.TYPE) {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        Literals.INT(0)));
            } else if (target.getReturnType() == Double.TYPE) {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        Literals.DOUBLE(0.0D)));
            } else if (target.getReturnType() == Float.TYPE) {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        Literals.FLOAT(0.0F)));
            } else if (target.getReturnType() == Long.TYPE) {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(),
                        Literals.LONG(0)));
            } else {
                return Instructions.fromPart(Factories.returnValue(target.getReturnType(), Literals.NULL));
            }
        }
    }
}