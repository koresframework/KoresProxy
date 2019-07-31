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
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.iutils.box.IMutableBox;

import java.util.Collections;

/**
 * Delegates all methods that are present in {@link #getTargetClass() target class} a instance
 * stored in {@link #mutableBox}.
 */
public class MutableInstance extends WrappedInstance {

    private final IMutableBox<?> mutableBox;

    /**
     * Creates wrapped instance direct invocation.
     *
     * @param mutableBox  Box with instance to delegate to.
     * @param targetClass Type of the wrapped object. All methods of this type that appears in proxy
     *                    class will be overwritten with delegation.
     */
    public MutableInstance(IMutableBox<?> mutableBox, Class<?> targetClass) {
        super(targetClass);
        this.mutableBox = mutableBox;
    }


    @Override
    protected Instruction evaluate(Instruction wrapper) {
        return InvocationFactory.invokeInterface(IMutableBox.class,
                wrapper,
                "getValue",
                Factories.typeSpec(Object.class),
                Collections.emptyList());
    }

    @Override
    protected Class<?> getWrapperType() {
        return IMutableBox.class;
    }

    @Override
    protected Object getWrapper() {
        return this.mutableBox;
    }
}