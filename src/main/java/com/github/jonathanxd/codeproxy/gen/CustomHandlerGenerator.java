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
package com.github.jonathanxd.codeproxy.gen;

import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.MutableCodeSource;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.VariableDeclaration;
import com.github.jonathanxd.codeproxy.ProxyData;
import com.github.jonathanxd.codeproxy.handler.InvocationHandler;
import com.github.jonathanxd.codeproxy.info.MethodInfo;

import java.lang.reflect.Method;

/**
 * Custom handler generator, can be used to generate interception of invocation of {@link
 * InvocationHandler#invoke(Object, MethodInfo, Object[], ProxyData)} or to generate full body of
 * the method.
 *
 * The main difference between {@link CustomHandlerGenerator} and {@link CustomGen} is that {@link
 * CustomHandlerGenerator} is appended before invocation of {@link InvocationHandler#invoke(Object,
 * MethodInfo, Object[], ProxyData)} and can generate full body of handler and {@link CustomGen} is
 * appended after invocation of {@link InvocationHandler#invoke(Object, MethodInfo, Object[],
 * ProxyData)} and can intercept the returned value of the proxy method.
 *
 * {@link CustomHandlerGenerator} can also manually call {@link CustomGen#gen(Method,
 * MethodDeclaration, VariableDeclaration)} of {@link CustomGen Custom Generators} through {@link
 * GenEnv#callCustomGenerators(VariableDeclaration, MutableCodeSource)}, this is useful when {@link
 * GenEnv#invokeHandler} is set to {@code false}.
 *
 * The implementation must have an empty constructor or a INSTANCE field.
 */
public interface CustomHandlerGenerator {

    /**
     * Generates custom source for {@code target} proxy method.
     *
     * Obs: there is no limitation of instructions that can be added to returned source.
     *
     * All values provided to this method is accessible through {@link GenEnv}.
     *
     * @param target            Target proxy method.
     * @param methodDeclaration Declaration of the method that overrides proxy method in proxy
     *                          class.
     * @return Custom source, appended at the start of proxy method.
     */
    CodeSource handle(Method target, MethodDeclaration methodDeclaration, GenEnv env);

}
