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

import com.github.jonathanxd.codeproxy.CodeProxy;
import com.github.jonathanxd.codeproxy.internals.Util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class ProxyTest {

    @Test
    public void test() {

        MyClass myClass = new MyClass("OR");

        MyClass apb = CodeProxy.newProxyInstance(this.getClass().getClassLoader(), MyClass.class, (proxy, method, args, info) -> {

            Thread.dumpStack();

            System.out.println(info);

            if (method.getName().equals("apb")) {
                return "OI";
            }

            if (method.getName().equals("getStr")) {
                Field str = MyClass.class.getDeclaredField("str");
                str.setAccessible(true);
                return str.get(proxy);
            }

            if (method.getName().equals("hashCode")) {
                return 7;
            }

            if (method.getName().equals("packagePrivate")) {
                return "Oops";
            }

            return method.resolveOrFail(myClass.getClass()).bindTo(myClass).invokeWithArguments(args);
        }, new Class[]{String.class}, new Object[]{"XS"});

        System.out.println(apb.apb());
        System.out.println(apb.getStr());
        System.out.println(apb.hashCode());
        System.out.println(apb.packagePrivate());

        Assert.assertEquals("apb.apb()", "OI", apb.apb());
        Assert.assertEquals("apb.getStr()", "XS", apb.getStr());

        if (Util.useModulesRules()) {
            Assert.assertEquals("apb.packagePrivate()", "VVS", apb.packagePrivate());
        } else {
            // This does not work in Java 9 because Util does not use
            // Private class loader methods to inject the class
            // And does not declared in the same package because it is not allowed in Java 9
            Assert.assertEquals("apb.packagePrivate()", "Oops", apb.packagePrivate());
        }
        Assert.assertEquals("apb.r()", myClass.r(0), apb.r(0));
        Assert.assertEquals("hashCode", 7, apb.hashCode());
        Assert.assertTrue("isProxy", CodeProxy.isProxy(apb));

    }

    public static class MyClass {

        private final String str;

        public MyClass(String str) {
            this.str = str;
        }

        public String apb() {
            return "ECHO";
        }

        public String getStr() {
            return str;
        }

        String packagePrivate() {
            return "VVS";
        }

        public int r(int a) {
            return 9;
        }

        public int v(int[] b) {
            return 9;
        }

        public boolean equals(Object o) {
            return false;
        }

    }

}
