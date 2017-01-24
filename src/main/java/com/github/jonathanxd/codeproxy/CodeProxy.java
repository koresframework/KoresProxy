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
package com.github.jonathanxd.codeproxy;

import com.github.jonathanxd.codeproxy.handler.InvocationHandler;
import com.github.jonathanxd.codeproxy.internals.ProxyGenerator;

public class CodeProxy {

    /**
     * Generate new proxy instance that extends {@code superClass}, implements {@code interfaces},
     * invoke super constructor, delegates invocations to {@code invocationHandler} and inject proxy
     * instance into {@code classLoader}.
     *
     * @param <T>               Type of proxy.
     * @param classLoader       Class loader to inject proxy instance.
     * @param superClass        Super class of Proxy.
     * @param interfaces        Interfaces to implements.
     * @param invocationHandler Handler to delegate invocations.
     * @param argTypes          Types of arguments of constructor of {@code superClass}.
     * @param args              Arguments to pass to constructor of {@code superClass}
     * @return Proxy instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ClassLoader classLoader,
                                         Class<T> superClass,
                                         Class<?>[] interfaces,
                                         InvocationHandler invocationHandler,
                                         Class<?>[] argTypes,
                                         Object[] args) {
        return (T) ProxyGenerator.create(new ProxyData(classLoader, interfaces, superClass, invocationHandler), argTypes, args);
    }

    /**
     * Generate new proxy instance that extends {@code superClass}, implements {@code interfaces},
     * delegates invocations to {@code invocationHandler} and inject proxy instance into {@code
     * classLoader}.
     *
     * @param classLoader       Class loader to inject proxy instance.
     * @param superClass        Super class of Proxy.
     * @param interfaces        Interfaces to implements.
     * @param invocationHandler Handler to delegate invocations.
     * @param <T>               Type of proxy.
     * @return Proxy instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ClassLoader classLoader,
                                         Class<T> superClass,
                                         Class<?>[] interfaces,
                                         InvocationHandler invocationHandler) {
        return CodeProxy.newProxyInstance(classLoader, superClass, interfaces, invocationHandler, new Class[0], new Object[0]);
    }

    /**
     * Generate new proxy instance that implements {@code interfaces},
     * delegates invocations to {@code invocationHandler} and inject proxy instance into {@code
     * classLoader}.
     *
     * @param classLoader       Class loader to inject proxy instance.
     * @param interfaces        Interfaces to implements.
     * @param invocationHandler Handler to delegate invocations.
     * @return Proxy instance.
     */
    public static Object newProxyInstance(ClassLoader classLoader,
                                          Class<?>[] interfaces,
                                          InvocationHandler invocationHandler) {
        return CodeProxy.newProxyInstance(classLoader, Object.class, interfaces, invocationHandler);
    }

    /**
     * Generate new proxy instance that extends {@code superClass}, invoke super constructor,
     * delegates invocations to {@code invocationHandler}, and inject proxy instance into {@code
     * classLoader}.
     *
     * @param classLoader       Class loader to inject proxy instance.
     * @param superClass        Super class of Proxy.
     * @param invocationHandler Handler to delegate invocations.
     * @param argTypes          Types of arguments of constructor of {@code superClass}.
     * @param args              Arguments to pass to constructor of {@code superClass}
     * @param <T>               Type of proxy.
     * @return Proxy instance.
     */
    public static <T> T newProxyInstance(ClassLoader classLoader,
                                         Class<T> superClass,
                                         InvocationHandler invocationHandler,
                                         Class<?>[] argTypes,
                                         Object[] args) {
        return CodeProxy.newProxyInstance(classLoader, superClass, new Class[0], invocationHandler, argTypes, args);
    }

    /**
     * Generate new proxy instance that extends {@code superClass},
     * delegates invocations to {@code invocationHandler} and inject proxy instance into {@code
     * classLoader}.
     *
     * @param classLoader       Class loader to inject proxy instance.
     * @param superClass        Super class of Proxy.
     * @param invocationHandler Handler to delegate invocations.
     * @param <T>               Type of proxy.
     * @return Proxy instance.
     */
    public static <T> T newProxyInstance(ClassLoader classLoader, Class<T> superClass, InvocationHandler invocationHandler) {
        return CodeProxy.newProxyInstance(classLoader, superClass, new Class[0], invocationHandler);
    }

    /**
     * Returns true if {@code o} is a {@link CodeProxy} instance.
     *
     * @param o Object to check.
     * @return True if {@code o} is a {@link CodeProxy} instance.
     */
    public static boolean isProxy(Object o) {
        return ProxyGenerator.isProxy(o);
    }

    /**
     * Gets the {@link InvocationHandler} of a {@link CodeProxy}.
     *
     * @param o Object to get the handler.
     * @return The {@link InvocationHandler} of {@link CodeProxy}.
     */
    public static InvocationHandler getHandler(Object o) {
        return ProxyGenerator.getInvocationHandler(o);
    }

    /**
     * Gets the {@link ProxyData} of a {@link CodeProxy}.
     *
     * @param o Object to get the handler.
     * @return The {@link ProxyData} of {@link CodeProxy}.
     */
    public static ProxyData getProxyData(Object o) {
        return ProxyGenerator.getProxyData(o);
    }
}
