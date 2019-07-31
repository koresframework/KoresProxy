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

import com.github.jonathanxd.iutils.io.DelegatePrintStream;
import com.github.jonathanxd.koresproxy.KoresProxy;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class SuperInvocationTest {
    @Test
    public void superInvocationTest() {
        final TestConsumer testConsumer = new TestConsumer();
        final DelegatePrintStream stream = new DelegatePrintStream(testConsumer);

        Printer printer = KoresProxy.newProxyInstance(builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .interfaces(Printer.class)
                        .invocationHandler((instance, methodInfo, args, proxyData) -> {
                            if (methodInfo.hasDefaultImplementation()) {
                                return methodInfo.invokeSuper(instance, args);
                            }
                            if (args.length > 0) {
                                stream.println(args[0]);
                            }
                            return null;
                        }));


        printer.println(9);
        printer.println("H");

        Assert.assertEquals("9", testConsumer.poll());
        Assert.assertEquals("H", testConsumer.poll());
    }

    public interface Printer {
        void println(String s);

        default void println(int i) {
            this.println(String.valueOf(i));
        }
    }

    static class TestConsumer implements Consumer<String> {
        private final Queue<String> queue = new LinkedList<>();

        @Override
        public void accept(String s) {
            s = s.replace('\n', '\0');

            if (!s.equals("\n") && !s.isEmpty())
                this.queue.add(s);
        }

        public String poll() {
            return this.queue.poll();
        }
    }
}
