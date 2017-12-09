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

import java.lang.reflect.Method;

/**
 * Target element to be invoked.
 */
public final class Target {

    /**
     * Default behavior of CodeProxy (delegate invocation handler)
     */
    public static final int DEFAULT_BEHAVIOR = -1;

    /**
     * Invokes a method of current instance, not every custom supports it.
     */
    public static final int SELF = -2;

    /**
     * The target instance, this instance if extracted from {@link DirectToResolveMethod#instances}
     * by index. To invoke self instance, use {@link Target#SELF}.
     */
    private final int instance;

    /**
     * Target method to invoke. If the method is static, the instance will not be resolved using
     * {@link #instance} index and the method will be invoked directly.
     */
    private final Method method;

    public Target(int instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    /**
     * Gets the instance index to resolve instance object.
     *
     * @return Instance index to resolve instance object.
     */
    public int getInstance() {
        return this.instance;
    }

    /**
     * Gets method to invoke.
     *
     * @return Method to invoke.
     */
    public Method getMethod() {
        return this.method;
    }
}
