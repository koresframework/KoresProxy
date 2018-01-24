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
package com.github.jonathanxd.codeproxy.gen;

import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.VariableDeclaration;
import com.github.jonathanxd.codeproxy.ProxyData;
import com.github.jonathanxd.codeproxy.handler.InvocationHandler;
import com.github.jonathanxd.codeproxy.info.MethodInfo;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Custom generator, can be used to add additional handling to invocation. The {@link CodeSource
 * source} is appended after invocation of {@link InvocationHandler#invoke(Object, MethodInfo,
 * Object[], ProxyData)}.
 *
 * If you want to intercept call to {@link InvocationHandler#invoke(Object, MethodInfo, Object[],
 * ProxyData)}, use {@link CustomHandlerGenerator}, which allows you to intercept {@link
 * InvocationHandler#invoke(Object, MethodInfo, Object[], ProxyData)} call or generate full body of
 * the proxy method.
 *
 * The implementation must have an empty constructor or a INSTANCE field.
 */
public interface CustomGen {

    /**
     * Generates custom source for {@code target} proxy method.
     *
     * Obs: there is no limitation of instructions that can be added to returned source.
     *
     * @param target            Target proxy method.
     * @param methodDeclaration Declaration of the method that overrides proxy method in proxy
     *                          class.
     * @param returnVariable    Variable that contains return value. {@code null} if method return
     *                          type is void.
     * @return Custom source, appended after {@link InvocationHandler#invoke(Object, MethodInfo,
     * Object[], ProxyData)} method.
     */
    CodeSource gen(Method target, MethodDeclaration methodDeclaration, @Nullable VariableDeclaration returnVariable);

}
