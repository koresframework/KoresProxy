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
package com.github.jonathanxd.codeproxy.handler;

import com.github.jonathanxd.codeapi.base.TypeSpec;
import com.github.jonathanxd.codeproxy.ProxyData;
import com.github.jonathanxd.codeproxy.info.MethodInfo;

import java.lang.reflect.Type;

import kotlin.collections.CollectionsKt;

/**
 * This interface is used to handle invocation of CodeProxies.
 */
@FunctionalInterface
public interface InvocationHandler {

    InvocationHandler NULL = (ignored1, ignored2, ignored3, ignored4) -> null;

    /**
     * Handles the invocation of a method.
     *
     * You can also return {@link com.github.jonathanxd.codeproxy.InvokeSuper#INVOKE_SUPER} to
     * request a super invocation of the method in the proxy class context. (Only works if {@link
     * ProxyData} specified it during class generation)
     *
     * @param instance   Proxy instance.
     * @param methodInfo Information of context and called method.
     * @param args       Arguments passed to method.
     * @param proxyData  Information about proxy.
     * @return Value to return in proxy method.
     * @throws Throwable If penguins try to swim in lava.
     */
    Object invoke(Object instance, MethodInfo methodInfo, Object[] args, ProxyData proxyData) throws Throwable;

    enum Info {
        ;
        public static final String METHOD_NAME = "invoke";
        public static final TypeSpec SPEC = new TypeSpec(
                Object.class,
                CollectionsKt.<Type>listOf(
                        Object.class,
                        MethodInfo.class,
                        Object[].class,
                        ProxyData.class
                ));
    }

}
