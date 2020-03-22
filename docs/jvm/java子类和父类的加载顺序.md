# 子类和父类的加载顺序
```java
class Father {
    private int i = test();
    private static int j = method();

    static {
        System.out.print("(1)");
    }

    Father() {
        System.out.print("(2)");
    }

    {
        System.out.print("(3)");
    }

    public int test() {
        System.out.print("(4)");
        return 1;
    }

    public static int method() {
        System.out.print("(5)");
        return 1;
    }
}

public class Son extends Father {
    private int i = test();
    private static int j = method();

    static {
        System.out.print("(6)");
    }

    Son() {
        System.out.print("(7)");
    }

    {
        System.out.print("(8)");
    }

    public int test() {
        System.out.print("(9)");
        return 1;
    }

    public static int method() {
        System.out.print("(10)");
        return 1;
    }

    public static void main(String[] args) {
        Son s1 = new Son();
        System.out.println();
        Son s2 = new Son();
    }
}
```

输出顺序是
(5)(1)(10)(6)(9)(3)(2)(9)(8)(7)<br/>
(9)(3)(2)(9)(8)(7)

> 先调用父类的<clinit>，再调用子类的<clinit>，然后是父类的<init>，最后是子类的<init>。其中<clinit>中包括了静态字段与静态构造块（method是静态方法，使用invokestatic  调用，静态绑定，编译时确定，所以调用的都是本类的method方法）。<init>中包括了实例字段的初始化、实例初始化块以及构造方法。（test方法的调用使用的是 invokevirtual 。涉及到多态了，由于实际类型都是Son，使用都是调用子类的test方法）。<clinit>只会被调用一次，<init>每次生成一个对象时都会被调用。