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
import com.github.jonathanxd.koresproxy.InvokeSuper;

import org.junit.Assert;

public class DefaultTest {

    @org.junit.Test
    public void test() {


        Itf itf = (Itf) KoresProxy.newProxyInstance(this.getClass().getClassLoader(), Object.class, new Class[] { Itf.class }, (proxy, method, args, info) -> {

            if(method.getName().equals("h"))
                return 7;

            return InvokeSuper.INVOKE_SUPER;
        });

        Assert.assertEquals("itf.h()", 7, itf.h());
        Assert.assertEquals("itf.x()", "Hello", itf.x());

    }

    public class Hey extends Object {
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }

    public static interface Itf {
        int h();

        default String x() {
            return "Hello";
        }
    }


}
