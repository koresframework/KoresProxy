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
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.gen.DirectInvocationCustom;
import com.github.jonathanxd.iutils.collection.Collections3;

import java.lang.reflect.Method;
import java.util.List;

public abstract class SimpleWrappedInstance implements DirectInvocationCustom {

    /**
     * Creates wrapped instance direct invocation.
     */
    public SimpleWrappedInstance() {
    }

    /**
     * Returns evaluation of {@code wrapper} that returns the instance to delegate method.
     *
     * @param wrapper Instruction that accesses the wrapper object.
     * @return Evaluation of {@code wrapper} that returns the instance to delegate method.
     */
    protected abstract Instruction evaluate(Instruction wrapper);

    /**
     * Gets the type of the wrapper. This type is used to create a field for the wrapper.
     *
     * @return Type of the wrapper.
     */
    protected abstract Class<?> getWrapperType();

    /**
     * Gets the instance of the wrapper. This instance will be used to create proxy instance and to
     * get wrapped instance.
     *
     * @return Instance of the wrapper.
     */
    protected abstract Object getWrapper();

    @Override
    public List<Property> getAdditionalProperties() {
        return Collections3.listOf(
                new Property(new VariableRef(this.getWrapperType(), "wrapper"), null)
        );
    }

    @Override
    public List<Object> getValueForConstructorProperties() {
        return Collections3.listOf(this.getWrapper());
    }

    @Override
    public abstract boolean generateSpecCache(Method m);

    @Override
    public abstract List<CustomHandlerGenerator> getCustomHandlerGenerators();

}