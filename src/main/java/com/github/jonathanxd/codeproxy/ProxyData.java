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
import com.github.jonathanxd.iutils.string.ToStringHelper;

import java.util.Arrays;
import java.util.Objects;

/**
 * Hold information about proxy class. This class is used to generate proxy classes
 * and is stored in the generated class.
 */
public class ProxyData {

    /**
     * Class loader used to load proxy class (this is not required since you can simple call {@link
     * Class#getClassLoader()}, but this field is not removed because we use this information in
     * generation and reuse the instance in proxy creation.)
     */
    private final ClassLoader classLoader;

    /**
     * Interfaces which proxy implements.
     */
    private final Class<?>[] interfaces;

    /**
     * Super class of proxy class. The features is very limited, if proxy system has access to
     * package where super class is defined, then the proxy is defined in this package and have
     * access to package-private methods, if not, it has only access to protected and public
     * methods. CodeProxies can't override private methods.
     */
    private final Class<?> superClass;

    /**
     * Handler of invocations.
     */
    private final InvocationHandler handler;

    public ProxyData(ClassLoader classLoader, Class<?>[] interfaces, Class<?> superClass, InvocationHandler handler) {
        this.classLoader = classLoader;
        this.interfaces = interfaces;
        this.superClass = superClass;
        this.handler = handler;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public Class<?>[] getInterfaces() {
        return this.interfaces.clone();
    }

    public Class<?> getSuperClass() {
        return this.superClass;
    }

    public InvocationHandler getHandler() {
        return this.handler;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ProxyData) {
            ProxyData proxyData = (ProxyData) obj;
            return this.getClassLoader().equals(proxyData.getClassLoader())
                    //&& this.getHandler().equals(proxyData.getHandler())
                    && Arrays.equals(this.getInterfaces(), proxyData.getInterfaces())
                    && this.getSuperClass().equals(proxyData.getSuperClass());

        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClassLoader(), this.getSuperClass(), Arrays.hashCode(this.getInterfaces()));
    }

    @Override
    public String toString() {
        return ToStringHelper.defaultHelper(this.getClass().getSimpleName())
                .add("classLoader", this.getClassLoader().toString())
                .add("superClass", this.getSuperClass().toString())
                .add("interfaces", Arrays.toString(this.getInterfaces()))
                .add("invocationHandler", this.getHandler().toString())
                .toString();
    }
}
