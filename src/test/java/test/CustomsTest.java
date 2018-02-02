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
package test;

import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.KoresPartKt;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.operator.Operators;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;
import com.github.jonathanxd.koresproxy.KoresProxy;
import com.github.jonathanxd.koresproxy.InvokeSuper;
import com.github.jonathanxd.koresproxy.gen.CustomHandlerGenerator;
import com.github.jonathanxd.koresproxy.gen.DirectInvocationCustom;
import com.github.jonathanxd.koresproxy.gen.GenEnv;
import com.github.jonathanxd.koresproxy.gen.direct.ArgsResolver;
import com.github.jonathanxd.koresproxy.gen.direct.DirectToFunction;
import com.github.jonathanxd.koresproxy.gen.direct.DirectToResolveMethod;
import com.github.jonathanxd.koresproxy.gen.direct.InvokeValidator;
import com.github.jonathanxd.koresproxy.gen.direct.MutableInstance;
import com.github.jonathanxd.koresproxy.gen.direct.Target;
import com.github.jonathanxd.koresproxy.handler.InvocationHandler;
import com.github.jonathanxd.iutils.annotation.Named;
import com.github.jonathanxd.iutils.box.IMutableBox;
import com.github.jonathanxd.iutils.box.MutableBox;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.kt.EitherUtilKt;
import com.github.jonathanxd.iutils.map.MapUtils;
import com.github.jonathanxd.iutils.object.Try;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kotlin.collections.ArraysKt;

public class CustomsTest {

    static Instruction get(Instruction receiver, String elem) {
        return InvocationFactory.invokeInterface(Map.class,
                Factories.cast(KoresPartKt.getType(receiver), Map.class, receiver),
                "get",
                Factories.typeSpec(Object.class, Object.class),
                Collections.singletonList(Literals.STRING(elem)));
    }

    @org.junit.Test
    public void test() {

        InvocationHandler myHandler = (proxy, method, args, info) -> {

            if (method.getName().equals("h"))
                return 7;

            if (method.getName().equals("v"))
                return 15;

            return InvokeSuper.INVOKE_SUPER;
        };

        Itf itf = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
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

        Wip wip = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Wip.class)
                        .addCustom(new DirectInvocationCustom.Static(StaticBoom.class))
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

        Wip wip = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Wip.class)
                        .addCustom(new DirectInvocationCustom.Instance(new Boom()))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myHandler));


        wip.put("name", "WIP");
        Assert.assertEquals("wip.getString(\"name\")", "WIP", wip.getString("name"));
        Assert.assertEquals("wip.getInt(\"n\")", -1, wip.getInt("n"));

    }

    @org.junit.Test
    public void testMultiInstance() {

        InvocationHandler myHandler = (proxy, method, args, info) -> {
            return InvokeSuper.INVOKE_SUPER;
        };

        A a = () -> 7;
        B b = () -> 9;

        P origin = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(P.class)
                        .addCustom(new DirectToFunction(
                                Collections3.listOf(
                                        args -> 8,
                                        args -> {
                                            throw new AbstractMethodError();
                                        }
                                ),
                                m -> m.getName().equals("n") ? 0 : m.getDeclaringClass() != Object.class ? 1 : -1
                        ))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myHandler)
        );

        P p = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(A.class)
                        .addInterface(B.class)
                        .addInterface(P.class)
                        .addCustom(new DirectInvocationCustom.MultiInstanceResolved(
                                Collections3.listOf(a, b, origin),
                                m -> {
                                    if (m.getName().equals("a"))
                                        return 0;
                                    if (m.getName().equals("x"))
                                        return 1;
                                    if (m.getName().equals("n"))
                                        return 2;
                                    return -1;
                                },
                                i -> i == 0 ? A.class : i == 1 ? B.class : /*i == 2 ?*/ P.class))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myHandler));


        Assert.assertEquals(7, p.a());
        Assert.assertEquals(9, p.x());
        Assert.assertEquals(8, p.n());

    }

    @org.junit.Test
    public void testDirectToResolveMethod() {
        InvocationHandler myInvocationHandler = (proxy, method, args, info) -> {
            return InvokeSuper.INVOKE_SUPER;
        };

        MyHandler myHandler = new MyHandler();

        Method onHello =
                EitherUtilKt.getRightOrFail(Try.TryEx(() -> MyHandler.class.getDeclaredMethod("onHello", String.class)));

        Wello origin = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Wello.class)
                        .addCustom(new DirectToResolveMethod(
                                Collections3.listOf(myHandler),
                                m -> m.getName().equals("onEvent")
                                        ? new Target(0, onHello)
                                        : new Target(Target.DEFAULT_BEHAVIOR, m),
                                i -> i == 0 ? MyHandler.class : Wello.class))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myInvocationHandler)
        );

        Assert.assertEquals("Hello-", origin.onEvent("Hello"));

        try {
            origin.onEvent(9);
            Assert.fail("ClassCastException expected.");
        } catch (ClassCastException ignored) {
        }

    }

    @org.junit.Test
    public void testDirectToResolveMethodWF() {
        InvocationHandler myInvocationHandler = (proxy, method, args, info) -> {
            return null;
        };

        MyHandler2 myHandler = new MyHandler2();

        Method onHello =
                EitherUtilKt.getRightOrFail(Try.TryEx(() -> MyHandler2.class.getDeclaredMethod("onHello",
                        String.class, Integer.class)));

        Wello origin = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .addInterface(Wello.class)
                        .addCustom(new DirectToResolveMethod(
                                Collections3.listOf(myHandler),
                                m -> m.getName().equals("onEvent")
                                        ? new Target(0, onHello)
                                        : new Target(Target.DEFAULT_BEHAVIOR, m),
                                i -> i == 0 ? MyHandler2.class : Wello.class,
                                new MyArgsResolver(),
                                new MyInvokeValidator()
                        ))
                        .addCustomGenerator(InvokeSuper.class)
                        .invocationHandler(myInvocationHandler)
        );

        Assert.assertEquals("WF -> 9", origin.onEvent(MapUtils.mapOf(
                "a", "WF",
                "b", 9
        )));
        Assert.assertEquals(null, origin.onEvent(MapUtils.mapOf(
                "a", "WF",
                "b", 9L
        )));
    }

    static abstract class Greeter {
        abstract String hello();
    }

    @Test
    public void mutableInstance() {

        class AGreeter extends Greeter {
            @Override
            String hello() {
                return "A";
            }
        }
        class BGreeter extends Greeter {
            @Override
            String hello() {
                return "B";
            }
        }

        IMutableBox<Greeter> myGreeter = new MutableBox<>();

        Greeter greeter = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
                builder.classLoader(this.getClass().getClassLoader())
                        .invocationHandler(InvocationHandler.NULL)
                        .superClass(Greeter.class)
                        .addCustom(new MutableInstance(myGreeter, Greeter.class))
                        .addCustomGenerator(InvokeSuper.class));

        myGreeter.set(new AGreeter());

        Assert.assertEquals("A", greeter.hello());

        myGreeter.set(new BGreeter());
        Assert.assertEquals("B", greeter.hello());
    }

    public interface A {
        int a();
    }

    public interface B {
        int x();
    }

    public interface P extends A, B {
        int n();
    }

    public interface Wello {
        Object onEvent(Object o);
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

    public static interface Itf {
        int h();

        int v();

        default String x() {
            return "Hello";
        }
    }

    public static class MyArgsResolver implements ArgsResolver {

        @Override
        public Instructions resolve(Method origin,
                                  MethodDeclaration proxyMethod,
                                  Method delegate,
                                  List<Instruction> arguments) {

            Instruction arg0 = arguments.get(0);

            List<String> names = ArraysKt.map(delegate.getParameters(), p -> p.getAnnotation(Named.class).value());

            arguments.clear();

            arguments.addAll(names.stream().map(s -> get(arg0, s)).collect(Collectors.toList()));

            return Instructions.empty();
        }


    }

    public static class MyInvokeValidator implements InvokeValidator {

        @Override
        public List<Instruction> generateValidation(Method origin,
                                                        MethodDeclaration proxyMethod,
                                                        Method delegate,
                                                        List<Instruction> arguments) {
            List<String> names = ArraysKt.map(delegate.getParameters(), p -> p.getAnnotation(Named.class).value());

            Instruction map = ConversionsKt.getAccess(proxyMethod.getParameters()).get(0);

            List<Instruction> insns = Collections3.listOf(
                    Factories.checkTrue(Factories.isInstanceOf(map, Map.class))
            );

            for (String name : names) {
                insns.add(Operators.AND);
                insns.add(Factories.checkNotNull(get(map, name)));
            }

            for (int i = 0; i < names.size(); i++) {
                insns.add(Operators.AND);
                Class<?> aClass = delegate.getParameterTypes()[i];

                insns.add(Factories.checkTrue(Factories.isInstanceOf(get(map, names.get(i)), aClass)));
            }

            return insns;
        }
    }

    public static class MyHandler {
        public String onHello(String s) {
            return s + "-";
        }
    }

    public static class MyHandler2 {
        public String onHello(@Named("a") String s, @Named("b") Integer i) {
            return s + " -> " + i;
        }
    }

    public static class StaticBoom {
        private static final Map<String, Object> map = new HashMap<>();

        public static void put(String key, Object value) {
            map.put(key, value);
        }

        public static String getString(String key) {
            Object o = map.get(key);

            if (o instanceof String) {
                return (String) o;
            }

            return "";
        }
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

    public static class MyCustomHandler implements CustomHandlerGenerator {

        @Override
        public Instructions handle(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
            if (target.getName().equals("v")) {
                env.setMayProceed(false);
                env.setInvokeHandler(false);

                return Instructions.fromPart(Factories.returnValue(Integer.TYPE, Literals.INT(50)));
            }

            return Instructions.empty();
        }
    }

    public class Hey extends Object {
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }


}
