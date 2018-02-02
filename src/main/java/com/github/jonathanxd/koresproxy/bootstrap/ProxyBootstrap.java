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
package com.github.jonathanxd.koresproxy.bootstrap;

import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.common.MethodInvokeSpec;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.iutils.exception.RethrowException;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

/**
 * Provides a bootstrap that resolves the expected method based on the input instance (aka multiple
 * dispatch).
 */
public class ProxyBootstrap {
    public static final MethodTypeSpec BOOTSTRAP_SPEC = new MethodTypeSpec(
            ProxyBootstrap.class,
            "dispatch",
            Factories.typeSpec(CallSite.class,
                    MethodHandles.Lookup.class,
                    String.class,
                    MethodType.class
            )
    );
    public static final MethodInvokeSpec BOOTSTRAP_IVK_SPEC = new MethodInvokeSpec(
            InvokeType.INVOKE_STATIC,
            BOOTSTRAP_SPEC
    );
    private static final MethodHandle FALLBACK;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static {
        try {
            FALLBACK = LOOKUP.findStatic(
                    ProxyBootstrap.class,
                    "resolve",
                    MethodType.methodType(Object.class, LazyCallSite.class, Object[].class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw RethrowException.rethrow(e);
        }
    }

    public static CallSite dispatch(MethodHandles.Lookup caller,
                                    String name,
                                    MethodType type) {

        LazyCallSite lazyCallSite = new LazyCallSite(type, caller, name);

        MethodHandle handle = FALLBACK.bindTo(lazyCallSite).asCollector(Object[].class, type.parameterCount()).asType(type);

        lazyCallSite.setTarget(handle);

        return lazyCallSite;
    }

    private static Object resolve(LazyCallSite callSite, Object[] args) {
        try {
            MethodHandles.Lookup caller = callSite.getCallerLookup();
            String name = callSite.getName();

            Object instance = args[0];
            Class<?> instanceClass = instance.getClass();
            MethodType type = callSite.getTarget().type().dropParameterTypes(0, 1);

            MethodHandle resolved = caller.findVirtual(instanceClass, name, type);

            callSite.setTarget(resolved.asType(callSite.getTarget().type()));

            return resolved.invokeWithArguments(args);
        } catch (Throwable e) {
            throw RethrowException.rethrow(e);
        }
    }

    static class LazyCallSite extends MutableCallSite {

        private final MethodHandles.Lookup callerLookup;
        private final String name;

        public LazyCallSite(MethodType type, MethodHandles.Lookup callerLookup, String name) {
            super(type);
            this.callerLookup = callerLookup;
            this.name = name;
        }

        public LazyCallSite(MethodHandle target, MethodHandles.Lookup callerLookup, String name) {
            super(target);
            this.callerLookup = callerLookup;
            this.name = name;
        }

        public MethodHandles.Lookup getCallerLookup() {
            return this.callerLookup;
        }

        public String getName() {
            return this.name;
        }
    }

}
