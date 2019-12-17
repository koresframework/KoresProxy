/*
 *      KoresProxy - Proxy Pattern written on top of Kores! <https://github.com/JonathanxD/KoresProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2019 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
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

import com.github.jonathanxd.koresproxy.KoresProxy;
import com.github.jonathanxd.koresproxy.gen.DirectInvocationCustom;
import com.github.jonathanxd.koresproxy.gen.direct.DummyCustom;

import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class DummyCustomTest {

    @Test
    public void testSingleMethodGenerate() {
        Greeter greet = KoresProxy.newProxyInstance(builder ->
                builder.classLoader(Greeter.class.getClassLoader())
                        .interfaces(Greeter.class)
                        .invocationHandler((instance, methodInfo, args, proxyData) -> null)
                        .addCustom(DummyCustom.create(m -> Objects.equals(Greeter.class, m.getDeclaringClass())))
        );

        Greeter greet2 = KoresProxy.newProxyInstance(builder ->
                builder.classLoader(Greeter.class.getClassLoader())
                        .interfaces(Greeter.class)
                        .invocationHandler((instance, methodInfo, args, proxyData) -> null)
                        .addCustom(DummyCustom.create(m -> Objects.equals(Greeter.class, m.getDeclaringClass())))
        );

        greet.greet();
        Assert.assertNotNull(greet.toString());
        Assert.assertNotEquals(0, greet.hashCode());
        Assert.assertEquals(greet, greet);
        Assert.assertNotEquals(greet, greet2);
    }

    @Test
    public void testDummyDelegate() {
        Greeter greet = KoresProxy.newProxyInstance(builder ->
                builder.classLoader(Greeter.class.getClassLoader())
                        .interfaces(Greeter.class)
                        .invocationHandler((instance, methodInfo, args, proxyData) -> null)
                        .addCustom(DummyCustom.create(m -> Objects.equals(Greeter.class, m.getDeclaringClass())))
        );

        Greeter greet2 = KoresProxy.newProxyInstance(builder ->
                builder.classLoader(Greeter.class.getClassLoader())
                        .superClass(greet.getClass())
                        .interfaces(Greeter.class)
                        .invocationHandler((instance, methodInfo, args, proxyData) -> null)
                        .addCustom(new DirectInvocationCustom.Instance(greet))
        );

        greet.greet();
        Assert.assertNotNull(greet.toString());
        Assert.assertNotEquals(0, greet.hashCode());
        Assert.assertEquals(greet, greet);
        Assert.assertNotEquals(greet, greet2);
    }

    @Test
    public void testDummyDelegate2() {
        Greeter greet = KoresProxy.newProxyInstance(builder ->
                builder.classLoader(Greeter.class.getClassLoader())
                        .interfaces(Greeter.class)
                        .invocationHandler((instance, methodInfo, args, proxyData) -> null)
                        .addCustom(DummyCustom.create(m -> Objects.equals(Greeter.class, m.getDeclaringClass())))
        );

        Greeter greet2 = KoresProxy.newProxyInstance(builder ->
                builder.classLoader(Greeter.class.getClassLoader())
                        .superClass(greet.getClass())
                        .interfaces(Greeter.class)
                        .invocationHandler((instance, methodInfo, args, proxyData) -> null)
                        .addCustom(new DirectInvocationCustom.Instance(greet))
        );

        Greeter greet3 = KoresProxy.newProxyInstance(new Class[]{greet.getClass()}, new Object[]{greet}, builder ->
                builder.classLoader(Greeter.class.getClassLoader())
                        .superClass(greet2.getClass())
                        .interfaces(Greeter.class)
                        .invocationHandler((instance, methodInfo, args, proxyData) -> null)
                        .addCustom(new DirectInvocationCustom.Instance(greet2))
        );

        greet.greet();
        Assert.assertNotNull(greet.toString());
        Assert.assertNotEquals(0, greet.hashCode());
        Assert.assertEquals(greet, greet);
        Assert.assertNotEquals(greet, greet2);
        Assert.assertNotEquals(greet, greet3);
        Assert.assertNotEquals(greet2, greet3);
    }


    public interface Greeter {
        void greet();
    }
}
