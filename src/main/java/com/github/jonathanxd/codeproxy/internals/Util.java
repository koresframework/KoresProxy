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
package com.github.jonathanxd.codeproxy.internals;

import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.MutableCodeSource;
import com.github.jonathanxd.codeapi.Types;
import com.github.jonathanxd.codeapi.base.ArrayConstructor;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.MethodInvocation;
import com.github.jonathanxd.codeapi.builder.MethodDeclarationBuilder;
import com.github.jonathanxd.codeapi.common.CodeModifier;
import com.github.jonathanxd.codeapi.common.CodeParameter;
import com.github.jonathanxd.codeapi.common.TypeSpec;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.type.CodeType;
import com.github.jonathanxd.codeapi.util.CodePartUtil;
import com.github.jonathanxd.codeproxy.ProxyData;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kotlin.collections.CollectionsKt;

public class Util {

    static CodePart methodToReflectInvocation(Method m) {

        return CodeAPI.invokeVirtual(Class.class, Literals.CLASS(m.getDeclaringClass()), "getDeclaredMethod",
                new TypeSpec(
                        CodeAPI.getJavaType(Method.class),
                        CollectionsKt.<CodeType>listOf(CodeAPI.getJavaType(String.class), CodeAPI.getJavaType(Class[].class))
                ),
                CollectionsKt.listOf(Literals.STRING(m.getName()), Util.parametersToArrayCtr(m.getParameterTypes())));
    }

    private static ArrayConstructor parametersToArrayCtr(Class<?>[] parameterTypes) {

        List<CodePart> arguments = Arrays.stream(parameterTypes).map(Literals::CLASS).collect(Collectors.toList());

        return CodeAPI.arrayConstruct(Types.CLASS.toArray(1), new CodePart[]{Literals.INT(parameterTypes.length)},
                arguments);
    }

    static MethodDeclaration fromMethod(Method m) {
        return MethodDeclarationBuilder.builder()
                .withName(m.getName())
                .withModifiers(EnumSet.of(CodeModifier.PUBLIC))
                .withParameters(Util.fromParameters(m.getParameters()))
                .withReturnType(CodeAPI.getJavaType(m.getReturnType()))
                .withBody(new MutableCodeSource())
                .build();
    }

    static List<CodeParameter> fromParameters(Parameter[] parameters) {
        return Arrays.stream(parameters).map(parameter -> new CodeParameter(CodeAPI.getJavaType(parameter.getType()), parameter.getName())).collect(Collectors.toList());
    }

    static Stream<CodePart> fromParametersToArgs(Stream<CodeParameter> parameters) {
        return parameters.map(parameter ->
                CodeAPI.accessLocalVariable(parameter.getType(), parameter.getName())
        );
    }

    static Stream<CodePart> cast(Stream<CodePart> stream, CodeType target) {
        return stream.map(argument -> {
            CodeType type = CodePartUtil.getType(argument);

            if (type.isArray())
                return argument;

            if (type.compareTo(target) != 0) {
                return CodeAPI.cast(type, target, argument);
            }

            return argument;
        });
    }

    static CodePart constructProxyData(ProxyData proxyData, CodeType IH_TYPE, String IH_NAME) {

        List<CodePart> arguments = Arrays.stream(proxyData.getInterfaces()).map(Literals::CLASS).collect(Collectors.toList());

        ArrayConstructor arrayConstructor = CodeAPI.arrayConstruct(Types.CLASS.toArray(1), new CodePart[]{
                Literals.INT(proxyData.getInterfaces().length)
        }, arguments);

        return CodeAPI.invokeConstructor(CodeAPI.getJavaType(ProxyData.class),
                CodeAPI.constructorTypeSpec(CodeAPI.getJavaType(ClassLoader.class), Types.CLASS.toArray(1), Types.CLASS, IH_TYPE),
                CollectionsKt.listOf(Util.getClassLoader_(), arrayConstructor, Literals.CLASS(proxyData.getSuperClass()), CodeAPI.accessThisField(IH_TYPE, IH_NAME))
        );
    }

    private static MethodInvocation getClassLoader_() {
        return CodeAPI.invokeVirtual(Class.class,
                Util.getClass_(), "getClassLoader", new TypeSpec(CodeAPI.getJavaType(ClassLoader.class)), Collections.emptyList());
    }

    private static MethodInvocation getClass_() {
        return CodeAPI.invokeVirtual(Object.class, CodeAPI.accessThis(), "getClass", new TypeSpec(CodeAPI.getJavaType(Class.class)), Collections.emptyList());
    }

    static Class<?> injectIntoClassLoader(ClassLoader classLoader, String name, byte[] bytes) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);

            defineClass.setAccessible(true);

            return (Class<?>) defineClass.invoke(classLoader, name, bytes, 0, bytes.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isEqual(Method o1, Method o2) {
        return o1.getName().equals(o2.getName())
                && o1.getReturnType().equals(o2.getReturnType())
                && Arrays.equals(o1.getParameterTypes(), o2.getParameterTypes());
    }

    static boolean contains(Collection<Method> methods, Method o1) {
        for (Method method : methods) {
            if (method != o1 && Util.isEqual(method, o1))
                return true;
        }

        return false;
    }
}
