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

  return method.invoke(origin, args);
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
  return method.invoke(cwcOrigin2, args);
}, new Class[] { String.class }, new Object[]{ cwcOrigin2.getName() });

Assert.assertEquals("getName", "Origin 2", cwc.getName());
 ```

#### Limitations

- CodeProxy only handles public and protected methods.
- CodeProxy only handles non-final methods.
- CodeProxy only generate proxies to classes that have accessible constructors.

#### CodeProxy compared to Java Proxies

CodeProxy support Super-classes and Interfaces inheritance, Java Proxies only supports Interfaces inheritance.
