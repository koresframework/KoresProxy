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
package test;

import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.base.InvokeType;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.factory.Factories;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeproxy.CodeProxy;
import com.github.jonathanxd.codeproxy.InvokeSuper;
import com.github.jonathanxd.codeproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.codeproxy.gen.DirectInvocationCustom;
import com.github.jonathanxd.codeproxy.gen.GenEnv;
import com.github.jonathanxd.codeproxy.handler.InvocationHandler;

import org.junit.Assert;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CustomsTest {

    @org.junit.Test
    public void test() {

        InvocationHandler myHandler = (proxy, method, args, info) -> {

            if (method.getName().equals("h"))
                return 7;

            if (method.getName().equals("v"))
                return 15;

            return InvokeSuper.INVOKE_SUPER;
        };

        Itf itf = CodeProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Itf.class)
                        .addCustomHandlerGenerator(MyCustomHandler.class)
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myHandler));


        Assert.assertEquals("itf.h()", 7, itf.h());
        Assert.assertEquals("itf.v()", 50, itf.v());
        Assert.assertEquals("itf.x()", "Hello", itf.x());

    }

    @org.junit.Test
    public void testStatic() {

        InvocationHandler myHandler = (proxy, method, args, info) -> {
            return InvokeSuper.INVOKE_SUPER;
        };

        Wip wip = CodeProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Wip.class)
                        .addCustom(new DirectInvocationCustom.Static(Boom.class))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myHandler));


        wip.put("name", "WIP");
        Assert.assertEquals("wip.getString(\"name\")", "WIP", wip.getString("name"));
        Assert.assertEquals("wip.getInt(\"n\")", -1, wip.getInt("n"));

    }

    @org.junit.Test
    public void testInstance() {

        InvocationHandler myHandler = (proxy, method, args, info) -> {
            return InvokeSuper.INVOKE_SUPER;
        };

        Wip wip = CodeProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Wip.class)
                        .addCustom(new DirectInvocationCustom.Instance(new Boom()))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myHandler));


        wip.put("name", "WIP");
        Assert.assertEquals("wip.getString(\"name\")", "WIP", wip.getString("name"));
        Assert.assertEquals("wip.getInt(\"n\")", -1, wip.getInt("n"));

    }

    public static class Boom {
        private final Map<String, Object> map = new HashMap<>();

        public void put(String key, Object value) {
            map.put(key, value);
        }

        public String getString(String key) {
            Object o = map.get(key);

            if (o instanceof String) {
                return (String) o;
            }

            return "";
        }
    }

    public interface Wip {

        default void put(String key, Object value) {

        }

        default String getString(String key) {
            return "";
        }

        default int getInt(String key) {
            return -1;
        }
    }

    public static class MyCustomHandler implements CustomHandlerGenerator {

        @Override
        public CodeSource handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
            if (target.getName().equals("v")) {
                env.setMayProceed(false);
                env.setInvokeHandler(false);

                return CodeSource.fromPart(Factories.returnValue(Integer.TYPE, Literals.INT(50)));
            }

            return CodeSource.empty();
        }
    }

    public static interface Itf {
        int h();

        int v();

        default String x() {
            return "Hello";
        }
    }

    public class Hey extends Object {
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }


}
