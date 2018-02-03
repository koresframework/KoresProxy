# KoresProxy

KoresProxy is a Proxy generator written on top of [Kores](https://github.com/JonathanxD/Kores).

#### Using KoresProxy

###### A basic Proxy example:

```java

public class MyClass {
  public int getNumber() {
    return 10;
  }
}

MyClass instance = KoresProxy.newProxyInstance(this.getClass().getClassLoader(), MyClass.class, (instance0, method, args, proxyData) -> {
  if (method.getName().equals("getNumber"))
    return 5;

  return null;
});

Assert.assertEquals("getNumber", 5, instance.getNumber());

```

###### Delegate to another instance

```java

public class MyClass {
  public int getNumber() {
    return 10;
  }
}

MyClass origin = new MyClass();
MyClass instance = KoresProxy.newProxyInstance(this.getClass().getClassLoader(), MyClass.class, (instance0, method, args, proxyData) -> {
  if (method.getName().equals("getNumber"))
    return 5;

  return method.resolveOrFail(origin.getClass()).bindTo(origin).invokeWithArguments(args);
});

Assert.assertEquals("getNumber", 5, instance.getNumber());
Assert.assertEquals("hash", origin.hashCode(), instance.hashCode());

```

###### Class with constructors


 ```java
public class ClassWithConstructor {
  private final String name;

  public ClassWithConstructor(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}

ClassWithConstructor cwcOrigin2 = new ClassWithConstructor("Origin 2");
ClassWithConstructor cwc = KoresProxy.newProxyInstance(this.getClass().getClassLoader(), ClassWithConstructor.class, (instance0, method, args, proxyData) -> {
  return method.resolveOrFail(cwcOrigin2.getClass()).bindTo(cwcOrigin2).invokeWithArguments(args);
}, new Class[] { String.class }, new Object[]{ cwcOrigin2.getName() });

Assert.assertEquals("getName", "Origin 2", cwc.getName());
 ```

#### Limitations

- KoresProxy only handles public, protected and package-private (for package private, see below) methods.
- KoresProxy only handles non-final methods.
- KoresProxy only generate proxies to classes that have accessible constructors.

#### KoresProxy compared to Java Proxies

KoresProxy support Super-classes and Interfaces inheritance, Java Proxies only supports Interfaces inheritance.

Since 2.1, KoresProxy uses `MethodInfo` to provide method information and delegation instead of Java methods.

# Java 9+

In Java 9 (or superior) KoresProxy works in a different way, instead of trying to inject classes in the `ProxyData.classLoader` using private inaccessible methods, it only injects when the `defineClass` method is public and if the method is not public, it creates a new class loader (`CodeClassLoader`) and loads the class with it. If the `ProxyData.classLoader` is a `CodeClassLoader`, it will use the instance instead of creating a new one. Also, KoresProxy does not override package-private methods in Java 9 nor defines the class in the same package as the target super class. You can disable this behavior using the option described below.

# VM options

Specify them using `-D` or defining using the `System.setProperty(String, String)`.

- koresproxy.saveproxies
  - Description: Save proxies generated classes and disassembled code in `gen/`.
  - Values: `true|false`
  - Default: `false`
  
- koresproxy.ignore_module_rules
  - Description: Ignore rules that applies to Java 9+, see the Java 9+ section above.
  - Values: `true|false`
  - Default: `false`

# Custom

KoresProxy support custom proxy method generators.

## CustomGen

A generator of `InvocationHandler` post-processing code.

### Let's try

Example:

```java
@Test
public void readmeCustoms() {
    InvocationHandler myHandler = (instance, methodInfo, args, proxyData) -> {
        if (methodInfo.getName().equals("hash"))
            return 2;
        else
            return InvokeSuper.INSTANCE;
    };

    Data data = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
            builder.classLoader(ReadmeCustomGen.class.getClassLoader())
                    .addCustomGenerator(InvokeSuper.class)
                    .addCustomGenerator(ReadmeCustomGen.class)
                    .addInterface(Data.class)
                    .invocationHandler(myHandler)
    );

    Assert.assertEquals(4, data.hash());
}

public interface Data {
    int hash();
}

public static class ReadmeCustomGen implements CustomGen {

    @NotNull
    @Override
    public Instructions gen(@NotNull Method target,
                            @NotNull MethodDeclaration methodDeclaration,
                            @Nullable VariableDeclaration returnVariable) {
        // Inserts: result = result * 2; after InvocationHandler.invoke
        if (returnVariable != null && target.getName().equals("hash")) {

            VariableAccess variableAccess = Factories.accessVariable(returnVariable);
            Cast cast = Factories.cast(returnVariable.getType(), Integer.TYPE, variableAccess);
            Operate operate = Factories.operate(cast, Operators.MULTIPLY, Literals.INT(2));
            Cast result = Factories.cast(Integer.TYPE, Object.class, operate);

            return Instructions.fromPart(
                    Factories.setVariableValue(returnVariable, result)
            );
        }

        return Instructions.empty();
    }
}
```

Note that you need to cast `returnVariable` value to `Integer.TYPE` before applying the operation, this is because `InvocationHandler#invoke` returns an `Object`, and you are manipulating this object, also you need to cast the operation result before assigning `returnVariable` to the value.

###### Generated method

The generated method looks something like:

```java
public int hash() {
    Object result = this.invocationHandler.invoke(this, METHOD5, new Object[0], this.proxyData);
    if (result instanceof InvokeSuper) {
        result = new Integer(super.hash());
    }
    
    result = ((Integer)result) * 2;
    
    return (Integer)result;
}
```

## CustomHandlerGenerator

Like `CustomGen`, but to add instructions before `InvocationHandler#invoke` invocation or to generate the full body of the method. To generate full body of method you need to disable the generation of `InvocationHandler` and `MayProceed` parameter of generator.

### GenEnv

Environment that provides additional information of generation and parameters to control instructions generation.

###### mayProceed

Parameter that defines whether generator should continue calling `CustomHandlerGenerator` to generate additional instructions before `InvocationHandler#invoke`.

###### invokeHandler

Parameter that defines whether the generator should generate invocation of `InvocationHandler#invoke` or not.

### Let's try

#### Modifying parameters

```java
@Test
public void readmeCustomHandlerGenerators() {
    InvocationHandler myHandler = (instance, methodInfo, args, proxyData) -> {
        if (methodInfo.getName().equals("calc"))
            return (Integer) args[0] + (Integer) args[1];
        else
            return InvokeSuper.INSTANCE;
    };

    Data data = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
            builder.classLoader(ReadmeCustomHandlerGenerators.class.getClassLoader())
                    .addCustomGenerator(InvokeSuper.class)
                    .addCustomHandlerGenerator(ReadmeCustomHGenerator.class)
                    .addInterface(Data.class)
                    .invocationHandler(myHandler)
    );

    Assert.assertEquals(((5 * 10) + (5 * 10)), data.calc(5, 5));
}

public interface Data {
    int calc(int a, int b);
}

public static class ReadmeCustomHGenerator implements CustomHandlerGenerator {

    @NotNull
    @Override
    public Instructions handle(@NotNull Method target, @NotNull MethodDeclaration methodDeclaration, @NotNull GenEnv env) {
        if (target.getName().equals("calc")) {
            KoresParameter p1 = methodDeclaration.getParameters().get(0);
            KoresParameter p2 = methodDeclaration.getParameters().get(1);
            VariableRef v1 = new VariableRef(p1.getType(), p1.getName());
            VariableRef v2 = new VariableRef(p2.getType(), p2.getName());

            VariableAccess access1 = Factories.accessVariable(v1);
            VariableAccess access2 = Factories.accessVariable(v2);

            return Instructions.fromVarArgs(
                    Factories.setVariableValue(v1, Factories.operate(access1, Operators.MULTIPLY, Literals.INT(10))),
                    Factories.setVariableValue(v2, Factories.operate(access2, Operators.MULTIPLY, Literals.INT(10)))
            );

        }

        return Instructions.empty();
    }
}
```

###### Generated method

The generated method looks something like:

```java
public int calc(int a, int b) {
    a *= 10;
    b *= 10;
    Object result = this.invocationHandler.invoke(this, Method5, new Object[]{a, b}, this.proxyData);
    
    if (result instanceof InvokeSuper) {
        result = super.calc(arg0, arg1);
    }
    
    return (Integer) result;
}
```

#### Entire method

```java
@Test
public void readmeCustomHandlerGenerators2() {
    InvocationHandler myHandler = (instance, methodInfo, args, proxyData) -> {
        if (methodInfo.getName().equals("calc"))
            return (Integer) args[0] + (Integer) args[1];
        else
            return InvokeSuper.INSTANCE;
    };

    Data data = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
            builder.classLoader(ReadmeCustomHandlerGenerators2.class.getClassLoader())
                    .addCustomGenerator(InvokeSuper.class)
                    .addCustomHandlerGenerator(ReadmeCustomHGenerator.class)
                    .addInterface(Data.class)
                    .invocationHandler(myHandler)
    );

    Assert.assertEquals(((5 * 5)), data.calc(5, 5));
}

public interface Data {
    int calc(int a, int b);
}

public static class ReadmeCustomHGenerator implements CustomHandlerGenerator {

    @NotNull
    @Override
    public Instructions handle(@NotNull Method target, @NotNull MethodDeclaration methodDeclaration, @NotNull GenEnv env) {
        if (target.getName().equals("calc")) {
            env.setMayProceed(false);
            env.setInvokeHandler(false);

            KoresParameter p1 = methodDeclaration.getParameters().get(0);
            KoresParameter p2 = methodDeclaration.getParameters().get(1);
            VariableRef v1 = new VariableRef(p1.getType(), p1.getName());
            VariableRef v2 = new VariableRef(p2.getType(), p2.getName());

            VariableAccess access1 = Factories.accessVariable(v1);
            VariableAccess access2 = Factories.accessVariable(v2);


            return Instructions.fromPart(
                    Factories.returnValue(Integer.TYPE, Factories.operate(access1, Operators.MULTIPLY, access2))
            );

        }

        return Instructions.empty();
    }
}
```

###### Generated method

The generated method looks something like:

```java
public int calc(int a, int b) {
    return a * b;
}
```

#### Entire method supporting CustomGen

To support `CustomGen` while generating entire method body, you should call `GenEnv#callCustomGenerators`, example:

```java
@Test
public void readmeCustomHandlerGenerators2() {
    InvocationHandler myHandler = (instance, methodInfo, args, proxyData) -> {
        if (methodInfo.getName().equals("calc"))
            return (Integer) args[0] + (Integer) args[1];
        else
            return InvokeSuper.INSTANCE;
    };

    Data data = KoresProxy.newProxyInstance(new Class[0], new Object[0], builder ->
            builder.classLoader(ReadmeCustomHandlerGenerators3.class.getClassLoader())
                    .addCustomGenerator(InvokeSuper.class)
                    .addCustomHandlerGenerator(ReadmeCustomHGenerator.class)
                    .addInterface(Data.class)
                    .invocationHandler(myHandler)
    );

    Assert.assertEquals(((5 * 5)), data.calc(5, 5));
}

public interface Data {
    int calc(int a, int b);
}

public static class ReadmeCustomHGenerator implements CustomHandlerGenerator {

    @NotNull
    @Override
    public Instructions handle(@NotNull Method target, @NotNull MethodDeclaration methodDeclaration, @NotNull GenEnv env) {
        if (target.getName().equals("calc")) {
            env.setMayProceed(false);
            env.setInvokeHandler(false);

            KoresParameter p1 = methodDeclaration.getParameters().get(0);
            KoresParameter p2 = methodDeclaration.getParameters().get(1);
            VariableRef v1 = new VariableRef(p1.getType(), p1.getName());
            VariableRef v2 = new VariableRef(p2.getType(), p2.getName());

            VariableAccess access1 = Factories.accessVariable(v1);
            VariableAccess access2 = Factories.accessVariable(v2);

            MutableInstructions instructions = MutableInstructions.create();
            VariableDeclaration result = VariableFactory.variable(Object.class, "result",
                    Factories.cast(Integer.TYPE, Object.class,
                            Factories.operate(access1, Operators.MULTIPLY, access2)));

            instructions.add(result);

            env.callCustomGenerators(result, instructions);

            VariableAccess access = Factories.accessVariable(result);

            instructions.add(Factories.returnValue(Integer.TYPE, Factories.cast(Object.class, Integer.TYPE, access)));

            return instructions;

        }

        return Instructions.empty();
    }
}
```

**Note**: I know, this is the worst use case for this feature, but it is only an example of how to support `CustomGen` while generating entire method body.

###### Generated method

The generated method looks something like:

```java
Object result = arg0 * arg1;

if (result instanceof InvokeSuper) {
    result = super.calc(arg0, arg1);
}

return (Integer) result;
```

## Custom

Custom is the master guy here, with `Custom` you can add fields to be initialized in constructor (with provided values), control whether methods should have their spec cached and provide `CustomGen`s and `CustomHandlerGenerator`s.

**Note**: The field name is not the same name as the name provided to `Property`, to get the field name of property use `Util#getAdditionalPropertyFieldName` with the property name or `Custom.Property#getFieldName`.

If you want examples of `Custom`s see [this package](https://github.com/JonathanxD/KoresProxy/blob/master/src/main/java/com/github/jonathanxd/koresproxy/gen/direct).

# Builtin customs

## INVOKE_SUPER | CustomGen

Builtin custom gen that allows `InvocationHandler` to invoke `super` method. To invoke a super method you need to return `InvokeSuper.INSTANCE` or `InvokeSuper.INVOKE_SUPER` as `InvocationHandler#invoke` result. 

## DirectToFunction / Custom

Directly invoke a Java 8 `Function` for each invoked method.

## DirectToResolveMethod / Custom

Directly invoke to a target method.

## DynamicLazyInstance / Custom

Delegates to evaluated instance of a lazy evaluator.
 
This uses a dynamic bootstrap to resolve type of evaluated instance without evaluating the object. This type is used to resolve method to invoke.

## LazyInstance / Custom

Delegates to evaluated instance of a lazy evaluator.

This uses specified static type to resolve methods.

## MutableInstance / Custom

Delegates invocations to a instance that can be changed. (A static base type is needed).

## DirectInvocationCustom.Static / Custom

Delegates invocations to static methods of a class.

## DirectInvocationCustom.Instance / Custom

Delegates invocations methods of an instance.

## DirectInvocationCustom.MultiInstanceResolved / Custom

Delegates invocations methods to different instances resolved based on methods.

# Known issues

## Custom

### ClassFormatError Invalid start_pc ...

- `java.lang.ClassFormatError: Invalid start_pc * in LocalVariableTable in class file *`

This happens when a `Custom` adds a return statement out-side a flow or inside a flow that is always reached without invoking `env.setInvokeHandler(false);`, example:

```java
public class MyCustomGen implements CustomHandlerGenerator {
    @Override
    public CodeSource gen(Method target, MethodDeclaration methodDeclaration, GenEnv env) {
        return CodeSource.fromPart(Factories.returnValue(target.getReturnType(),
            InvocationFactory.invokeSpecial(
                        target.getDeclaringClass(), Access.SUPER, target.getName(), methodDeclaration.getTypeSpec(),
                        methodDeclaration.getParameters().stream()
                            .map(ConversionsKt::toVariableAccess)
                            .collect(Collectors.toList())
                )
        ));
    }
}
```

This code will cause class proxy to fail to load with class format error, to solve that, you should add:

```
env.setInvokeHandler(false);
env.setMayProceed(false);
```

Before the `return`.
 
#### Why this happens?

This happens because without `env.setInvokeHandler(false)` KoresProxy will append invocation to `InvocationHandler` after the code generated by your *CustomHandlerGenerator*, resulting in dead code. CodeAPI-BytecodeWriter optimizers will remove this dead code, but optimizers does not update `LocalVariableTable`, meaning that local variable entries will point to a label that does not exists anymore in the bytecode, resulting in verifier throwing exception.

The `env.setMayProceed(false);` will only prevent others `Custom` to be run, this is necessary to ensure that no other `Custom` will add code after your (resulting in dead code too).