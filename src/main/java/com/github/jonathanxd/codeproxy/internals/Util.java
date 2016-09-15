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
package com.github.jonathanxd.codeproxy.internals;

import com.github.jonathanxd.codeapi.CodeAPI;
import com.github.jonathanxd.codeapi.CodePart;
import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.common.CodeArgument;
import com.github.jonathanxd.codeapi.common.CodeModifier;
import com.github.jonathanxd.codeapi.common.CodeParameter;
import com.github.jonathanxd.codeapi.common.TypeSpec;
import com.github.jonathanxd.codeapi.helper.Helper;
import com.github.jonathanxd.codeapi.helper.PredefinedTypes;
import com.github.jonathanxd.codeapi.impl.CodeMethod;
import com.github.jonathanxd.codeapi.interfaces.ArrayConstructor;
import com.github.jonathanxd.codeapi.interfaces.MethodInvocation;
import com.github.jonathanxd.codeapi.literals.Literals;
import com.github.jonathanxd.codeapi.types.CodeType;
import com.github.jonathanxd.codeproxy.ProxyData;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    static CodePart methodToReflectInvocation(Method m) {

        return CodeAPI.invokeVirtual(Class.class, Util.getClass_(), "getMethod", new TypeSpec(Helper.getJavaType(Method.class), String.class, Class[].class),
                CodeAPI.argument(Literals.STRING(m.getName())), CodeAPI.argument(Util.parametersToArrayCtr(m.getParameterTypes())));
    }

    static ArrayConstructor parametersToArrayCtr(Class<?>[] parameterTypes) {

        CodeArgument[] arguments = Arrays.stream(parameterTypes).map(aClass -> new CodeArgument(Literals.CLASS(aClass), Class.class)).toArray(CodeArgument[]::new);

        return CodeAPI.arrayConstruct(Class.class, new CodePart[]{Literals.INT(parameterTypes.length)},
                arguments);
    }

    static CodeMethod fromMethod(Method m) {
        return new CodeMethod(m.getName(),
                Collections.singletonList(CodeModifier.PUBLIC),
                Util.fromParameters(m.getParameters()),
                Helper.getJavaType(m.getReturnType()),
                new CodeSource());
    }

    static List<CodeParameter> fromParameters(Parameter[] parameters) {
        return Arrays.stream(parameters).map(parameter -> new CodeParameter(parameter.getName(), Helper.getJavaType(parameter.getType()))).collect(Collectors.toList());
    }

    static Stream<CodeArgument> fromParametersToArgs(Stream<CodeParameter> parameters) {
        return parameters.map(parameter ->
                new CodeArgument(Helper.accessLocalVariable(parameter.getName(), parameter.getRequiredType()), parameter.getRequiredType())
        );
    }

    static CodePart constructProxyData(ProxyData proxyData, CodeType IH_TYPE, String IH_NAME) {

        CodeArgument[] arguments = Arrays.stream(proxyData.getInterfaces()).map(Literals::CLASS).toArray(CodeArgument[]::new);

        ArrayConstructor arrayConstructor = CodeAPI.arrayConstruct(Class.class, new CodePart[]{
                Literals.INT(proxyData.getInterfaces().length)
        }, arguments);

        return CodeAPI.invokeConstructor(ProxyData.class,
                CodeAPI.argument(Util.getClassLoader_(), ClassLoader.class),
                CodeAPI.argument(arrayConstructor, Class[].class),
                CodeAPI.argument(Literals.CLASS(proxyData.getSuperClass()), Class.class),
                CodeAPI.argument(CodeAPI.accessStaticThisField(IH_TYPE, IH_NAME), IH_TYPE));
    }

    static MethodInvocation getClassLoader_() {
        return CodeAPI.invokeVirtual(Class.class,
                Util.getClass_(), "getClassLoader", new TypeSpec(Helper.getJavaType(ClassLoader.class)));
    }

    static MethodInvocation getClass_() {
        return CodeAPI.invokeVirtual(Object.class, Helper.accessThis(), "getClass", new TypeSpec(Helper.getJavaType(Class.class)));
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
}
