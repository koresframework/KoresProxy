/*
 *      CodeProxy - Proxy Pattern written on top of CodeAPI! <https://github.com/JonathanxD/CodeProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
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
package com.github.jonathanxd.codeproxy;

import com.github.jonathanxd.codeproxy.handler.InvocationHandler;
import com.github.jonathanxd.codeproxy.internals.ProxyGenerator;

public class CodeProxy {

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, Class<T> superClass, InvocationHandler invocationHandler, Class<?>[] argTypes, Object[] args) {
        return (T) ProxyGenerator.create(new ProxyData(classLoader, interfaces, superClass, invocationHandler), argTypes, args);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, Class<T> superClass, InvocationHandler invocationHandler) {
        return CodeProxy.newProxyInstance(classLoader, interfaces, superClass, invocationHandler, new Class[0], new Object[0]);
    }

    public static Object newProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler invocationHandler) {
        return CodeProxy.newProxyInstance(classLoader, interfaces, Object.class, invocationHandler);
    }

    public static <T> T newProxyInstance(ClassLoader classLoader, Class<T> superClass, InvocationHandler invocationHandler, Class<?>[] argTypes, Object[] args) {
        return CodeProxy.newProxyInstance(classLoader, new Class[0], superClass, invocationHandler, argTypes, args);
    }

    public static <T> T newProxyInstance(ClassLoader classLoader, Class<T> superClass, InvocationHandler invocationHandler) {
        return CodeProxy.newProxyInstance(classLoader, new Class[0], superClass, invocationHandler);
    }

    public static boolean isProxy(Object o) {
        return ProxyGenerator.isProxy(o);
    }

    public static InvocationHandler getHandler(Object o) {
        return ProxyGenerator.getInvocationHandler(o);
    }
}
