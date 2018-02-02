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
package com.github.jonathanxd.koresproxy.info;

import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.exception.RethrowException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Method specification holder.
 *
 * This class holds {@link MethodHandles.Lookup} which is defined in proxy class, name, return type
 * and parameter types of method.
 */
public final class MethodInfo {

    /**
     * Specification of constructor.
     */
    public static TypeSpec CONSTRUCTOR_SPEC = new TypeSpec(Void.TYPE,
            Collections3.listOf(MethodHandles.Lookup.class, Class.class, String.class, Class.class, Class[].class));

    /**
     * Lookup created in proxy context.
     */
    private final MethodHandles.Lookup lookup;

    /**
     * Class which declares the method.
     */
    private final Class<?> declaringClass;

    /**
     * Name of the method.
     */
    private final String name;

    /**
     * Method return type.
     */
    private final Class<?> returnType;

    /**
     * Method parameters.
     */
    private final List<Class<?>> parameterTypes;

    /**
     * Method parameters (cached array).
     */
    private final Class<?>[] parameterTypesArray;

    public MethodInfo(MethodHandles.Lookup lookup,
                      Class<?> declaringClass,
                      String name,
                      Class<?> returnType,
                      Class<?>[] parameterTypes) {

        this.lookup = lookup;
        this.declaringClass = declaringClass;
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = Collections.unmodifiableList(Arrays.asList(parameterTypes));
        this.parameterTypesArray = parameterTypes.clone();
    }

    /**
     * Gets {@link #lookup}.
     *
     * @return {@link #lookup}.
     */
    public MethodHandles.Lookup getLookup() {
        return this.lookup;
    }

    /**
     * Gets {@link #declaringClass}.
     *
     * @return {@link #declaringClass}.
     */
    public Class<?> getDeclaringClass() {
        return this.declaringClass;
    }

    /**
     * Gets {@link #name}.
     *
     * @return {@link #name}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets {@link #returnType}.
     *
     * @return {@link #returnType}.
     */
    public Class<?> getReturnType() {
        return this.returnType;
    }

    /**
     * Gets {@link #parameterTypes}.
     *
     * @return {@link #parameterTypes}.
     */
    public List<Class<?>> getParameterTypes() {
        return this.parameterTypes;
    }

    /**
     * Resolves the {@link MethodHandle} of this method in {@code target}.
     *
     * @param target Target class to find method.
     * @return Resolved method handle, or null if cannot be found.
     */
    public @Nullable
    MethodHandle resolve(Class<?> target) {
        try {
            return this.lookup.findVirtual(target, this.name, MethodType.methodType(returnType, parameterTypesArray));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Resolves the {@link MethodHandle} of this method in {@code target}.
     *
     * @param target Target class to find method.
     * @return Resolved method handle, or throw exception if cannot be found.
     */
    public @NotNull
    MethodHandle resolveOrFail(Class<?> target) {
        try {
            return this.lookup.findVirtual(target, this.name, MethodType.methodType(returnType, parameterTypesArray));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw RethrowException.rethrow(e);
        }
    }

    /**
     * Resolves the {@link MethodHandle} of this method in {@code target}.
     *
     * @param target Target class to find method.
     * @return Resolved method handle, or null if cannot be found.
     */
    public @Nullable
    MethodHandle resolveSpecial(Class<?> target, Class<?> specialClass) {
        try {
            return this.lookup.findSpecial(target, this.name, MethodType.methodType(returnType, parameterTypesArray), specialClass);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Resolves the {@link MethodHandle} of this method in {@code target}.
     *
     * @param target Target class to find method.
     * @return Resolved method handle, or throw exception if cannot be found.
     */
    public @NotNull
    MethodHandle resolveSpecialOrFail(Class<?> target, Class<?> specialClass) {
        try {
            return this.lookup.findSpecial(target, this.name, MethodType.methodType(returnType, parameterTypesArray), specialClass);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw RethrowException.rethrow(e);
        }
    }

    /**
     * Resolves the {@link MethodHandle} of this method in {@code target}.
     *
     * @param target Target class to find method.
     * @return Resolved method handle, or null if cannot be found.
     */
    public @Nullable
    MethodHandle resolveStatic(Class<?> target) {
        try {
            return this.lookup.findStatic(target, this.name, MethodType.methodType(returnType, parameterTypesArray));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Resolves the {@link MethodHandle} of this method in {@code target}.
     *
     * @param target Target class to find method.
     * @return Resolved method handle, or throw exception if cannot be found.
     */
    public @NotNull
    MethodHandle resolveStaticOrFail(Class<?> target) {
        try {
            return this.lookup.findStatic(target, this.name, MethodType.methodType(returnType, parameterTypesArray));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw RethrowException.rethrow(e);
        }
    }

    /**
     * Resolves the {@link Method} of this method info in {@code target}.
     *
     * @param target Target class to find method.
     * @return Resolve method or null if method cannot be found.
     */
    public @Nullable
    Method resolveToReflect(Class<?> target) {
        try {
            Method method = target.getMethod(this.getName(), this.parameterTypesArray);

            if (!method.isAccessible())
                method.setAccessible(true);

            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Resolves the {@link Method} of this method info in {@code target}.
     *
     * @param target Target class to find method.
     * @return Resolve method or throw exception if method cannot be found.
     */
    public @NotNull
    Method resolveToReflectOrFail(Class<?> target) {
        try {
            Method method = target.getMethod(this.getName(), this.parameterTypesArray);

            if (!method.isAccessible())
                method.setAccessible(true);

            return method;
        } catch (NoSuchMethodException e) {
            throw RethrowException.rethrow(e);
        }
    }

}
