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
import com.github.jonathanxd.kores.base.IfExpressionHolder;
import com.github.jonathanxd.kores.base.MethodDeclaration;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Generates a validation to the invocation of the {@code delegate}.
 */
public interface InvokeValidator {

    /**
     * Generates validation to invocation of {@code delegate}.
     *
     * @param origin      Origin method (method to generate {@code proxyMethod}).
     * @param proxyMethod Generated proxy method.
     * @param delegate    Method to delegate.
     * @param arguments   A copy of arguments to be used to invoke the method.
     * @return {@link IfExpressionHolder#getExpressions() If expression list} to be used to
     * determine if {@code delegate} method should be invoked or {@code default behavior} should
     * run.
     */
    List<Instruction> generateValidation(Method origin,
                                             MethodDeclaration proxyMethod,
                                             Method delegate,
                                             List<Instruction> arguments);

}
