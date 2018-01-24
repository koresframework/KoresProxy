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
package test;

import com.github.jonathanxd.codeproxy.CodeProxy;
import com.github.jonathanxd.codeproxy.InvokeSuper;
import com.github.jonathanxd.codeproxy.gen.direct.DynamicLazyInstance;
import com.github.jonathanxd.codeproxy.gen.direct.LazyInstance;
import com.github.jonathanxd.codeproxy.handler.InvocationHandler;
import com.github.jonathanxd.iutils.object.Lazy;

import org.junit.Assert;
import org.junit.Test;

import kotlin.text.Charsets;

public class LazyInstanceTest {

    public static Input createInput(String name) {

        return new Input() {
            byte[] nameBytes;
            private int pos = 0;

            @Override
            public byte read() {
                if (pos == 0) {
                    ++pos;
                    return 7;
                }

                if (pos == 1) {
                    ++pos;
                    nameBytes = name.getBytes(Charsets.UTF_8);
                    return (byte) nameBytes.length;
                }

                byte b = nameBytes[pos - 2];
                ++pos;
                return b;
            }
        };
    }

    @Test
    public void lazyInstanceTest() {
        InvocationHandler myInvocationHandler = (proxy, method, args, info) -> {
            return InvokeSuper.INVOKE_SUPER;
        };

        Lazy<Expensive> expensive = Lazy.lazy(() -> new Expensive(createInput("Hello")));

        Entity entity = CodeProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Entity.class)
                        .addCustom(new LazyInstance(expensive, Entity.class))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myInvocationHandler)
        );

        Assert.assertFalse(expensive.isEvaluated());
        Assert.assertEquals("Hello", entity.getName());
        Assert.assertTrue(expensive.isEvaluated());
    }

    @Test
    public void dynamicLazyInstanceTest() {
        InvocationHandler myInvocationHandler = (proxy, method, args, info) -> {
            return InvokeSuper.INVOKE_SUPER;
        };

        Lazy<Expensive> expensive = Lazy.lazy(() -> new Expensive(createInput("Hello")));

        Entity entity = CodeProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Entity.class)
                        .addCustom(new DynamicLazyInstance(expensive))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myInvocationHandler)
        );

        Assert.assertFalse(expensive.isEvaluated());
        Assert.assertEquals("Hello", entity.getName());
        Assert.assertEquals("Hello", entity.getName());
        Assert.assertTrue(expensive.isEvaluated());
    }

    public interface Entity {
        String getName();
    }

    public interface Input {
        byte read();
    }

    public static class Expensive implements Entity {

        private final Input in;
        private final String name;

        public Expensive(Input in) {
            this.in = in;

            StringBuilder name = new StringBuilder();
            byte b = this.in.read();

            if (b == 7) {
                byte size = this.in.read();
                byte[] bytes = new byte[size];

                for (int i = 0; i < size; ++i) {
                    bytes[i] = this.in.read();
                }

                name.append(new String(bytes, Charsets.UTF_8));
            }

            this.name = name.length() == 0 ? null : name.toString();
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
