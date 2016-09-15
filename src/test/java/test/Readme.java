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
package test;

import com.github.jonathanxd.codeproxy.CodeProxy;

import org.junit.Assert;

public class Readme {

    @org.junit.Test
    public void readme() {
        MyClass instance = CodeProxy.newProxyInstance(this.getClass().getClassLoader(), MyClass.class, (instance0, method, args, proxyData) -> {
            if (method.getName().equals("getNumber"))
                return 5;

            return null;
        });

        Assert.assertEquals("getNumber", 5, instance.getNumber());

        MyClass origin = new MyClass();
        MyClass instance2 = CodeProxy.newProxyInstance(this.getClass().getClassLoader(), MyClass.class, (instance0, method, args, proxyData) -> {
            if (method.getName().equals("getNumber"))
                return 5;

            return method.invoke(origin, args);
        });

        Assert.assertEquals("getNumber", 5, instance2.getNumber());
        Assert.assertEquals("hash", origin.hashCode(), instance2.hashCode());


        ClassWithConstructor cwcOrigin2 = new ClassWithConstructor("Origin 2");
        ClassWithConstructor cwc = CodeProxy.newProxyInstance(this.getClass().getClassLoader(), ClassWithConstructor.class, (instance0, method, args, proxyData) -> {
            return method.invoke(cwcOrigin2, args);
        }, new Class[] { String.class }, new Object[]{ cwcOrigin2.getName() });

        Assert.assertEquals("getName", "Origin 2", cwc.getName());


    }

    public static class ClassWithConstructor {
        private final String name;

        public ClassWithConstructor(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    public static class MyClass {
        public int getNumber() {
            return 10;
        }
    }

}
