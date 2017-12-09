# CodeProxy

CodeProxy is a Proxy generator written on top of [CodeAPI](https://github.com/JonathanxD/CodeAPI).

#### Using CodeProxy

###### A basic Proxy example:

```java

public class MyClass {
  public int getNumber() {
    return 10;
  }
}

MyClass instance = CodeProxy.newProxyInstance(this.getClass().getClassLoader(), MyClass.class, (instance0, method, args, proxyData) -> {
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
MyClass instance = CodeProxy.newProxyInstance(this.getClass().getClassLoader(), MyClass.class, (instance0, method, args, proxyData) -> {
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
ClassWithConstructor cwc = CodeProxy.newProxyInstance(this.getClass().getClassLoader(), ClassWithConstructor.class, (instance0, method, args, proxyData) -> {
  return method.resolveOrFail(cwcOrigin2.getClass()).bindTo(cwcOrigin2).invokeWithArguments(args);
}, new Class[] { String.class }, new Object[]{ cwcOrigin2.getName() });

Assert.assertEquals("getName", "Origin 2", cwc.getName());
 ```

#### Limitations

- CodeProxy only handles public, protected and package-private methods.
- CodeProxy only handles non-final methods.
- CodeProxy only generate proxies to classes that have accessible constructors.

#### CodeProxy compared to Java Proxies

CodeProxy support Super-classes and Interfaces inheritance, Java Proxies only supports Interfaces inheritance.

Since 2.1, CodeProxy uses `MethodInfo` to provide method information and delegation instead of Java methods.

# Java 9+

In Java 9 (or superior) CodeProxy works in a different way, instead of trying to inject classes in the `ProxyData.classLoader` using private inaccessible methods, it only injects when the `defineClass` method is public and if the method is not public, it creates a new class loader (`CodeClassLoader`) and loads the class with it. If the `ProxyData.classLoader` is a `CodeClassLoader`, it will use the instance instead of creating a new one. Also, CodeProxy does not override package-private methods in Java 9 nor defines the class in the same package as the target super class. You can disable this behavior using the option described below.

# VM options

Specify them using `-D` or defining using the `System.setProperty(String, String)`.

- codeproxy.saveproxies
  - Description: Save proxies generated classes and disassembled code in `gen/`.
  - Values: `true|false`
  - Default: `false`
  
- codeproxy.ignore_module_rules
  - Description: Ignore rules that applies to Java 9+, see the Java 9+ section above.
  - Values: `true|false`
  - Default: `false`
