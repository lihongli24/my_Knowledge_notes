# java 内部类知识

## java内部类的总类

### 成员内部类

> 成员内部类：定义在一个类中，和成员变量一样的形式

```java
public class Out {
    private int age = 1;
    public int count = 2;
    private void outPrint(){
        System.out.println("out out!!!");
    }
    private class Inner {
        public void print() {
            outPrint();
            System.out.println("age" + age + count);
        }
    }
    public static void main(String[] args){
        Inner inner = new Out().new Inner();
        inner.print();

    }
}

```

* 成员内部类是依附外部类而存在的,实例化成员内部类需要使用外部类的实例来new，像上面的new Out().new Inner();
* 内部类可以拥有private访问权限、protected访问权限、public访问权限及包访问权限。 外部想访问到的情况和成员变量一样
* 内部类可以访问外部类所有变量和方法，包括private,这个后面会讲到

> 不过要注意的是，当成员内部类拥有和外部类同名的成员变量或者方法时，会发生隐藏现象，即默认情况下访问的是成员内部类的成员。如果要访问外部类的同名成员，需要以下面的形式进行访问：
>
> ```java
> 外部类.this.成员变量
> 外部类.this.成员方法
> ```

### 局部内部类

> 局部内部类：局部内部类是定义在一个方法或者一个作用域里面的类，它和成员内部类的区别在于局部内部类的访问仅限于方法内或者该作用域内。 和局部变量一样

```java
@Data
public class Out2 {
    private String paramOut;
    private void outPrint() {
        System.out.println("out out!!!");
    }
    private void method1() {
        String params2 = "123";
        class Inner2 {
            private void print() {
                System.out.println(paramOut);
                System.out.println(params2);
                outPrint();
            }
        }
        Inner2 inner2 = new Inner2();
        inner2.print();
    }
    public static void main(String[] args) {
        Out2 out2 = new Out2();
        out2.setParamOut("11");
        out2.method1();
    }
}
```

上面示例中的Inner2就是一个局部内部类

* 局部内部类就像是方法里面的一个局部变量一样，是不能有public、protected、private以及static修饰符的。
* 内部类可以访问外部类所有变量和方法，包括private,这个后面会讲到
* 局部内部类能访问到局部代码块中的final变量，上面示例中的params2之所以能访问到是因为jdk8的***Effectively final***概念，它默认会是一个final修饰的变量。

### 匿名内部类

> 定义这个内部类的时候不需要定义这个类的类名，一般是继承某个类或者实现某个接口的情况下

```java
public interface RemoteInterface {

    void print();
}
```



```java
public class Out3 {
    private String name = "apple";
    private Integer age = 2;
	 
    private void method1() {
        RemoteInterface remoteInterface = new RemoteInterface() {
            @Override
            public void print() {
                System.out.println("name:" + name + ", age:" + age);
            }
        };
        remoteInterface.print();
    }
  	//lambda写法
    private void method2(){
        RemoteInterface remoteInterface = () ->{System.out.println("name:" + name + ", age:" + age);};
        remoteInterface.print();
    }
  
    public static void main(String[] args) {
        Out3 out3 = new Out3();
        out3.method1();
        out3.method2();
    }
}
```

* 匿名内部类适合用于创建那些仅需要一次使用的类

### 静态内部类

> 类的使用static来修饰，不依赖于外部类的class

```java
public class Out4 {
    private String name = "apple";
    private static Integer age = 12;
    public static class Inner4 {
        private void print() {
            System.out.println(age);
        }
    }
    public static void main(String[] args){
        Inner4 inner4 = new Inner4();
        inner4.print();
    }
}
```

* 可以访问外部类的静态变量
* 静态内部类属于外部类，而不是属于外部类的对象，所以实例化的时候不需要依赖外部类的对象，可以直接实例化。
* 生成静态内部类对象的方式：Outer.Inner inner = new Outer.Inner()。
* 只能访问外部类的静态成员变量或者静态方法。



## 内部类的作用

* 内部类可以很好的实现隐藏

 一般的非内部类，是不允许有 private 与protected权限的，但内部类可以

* 内部类拥有外围类的所有元素的访问权限

* 可是实现多重继承

* 可以避免修改接口而实现同一个类中两种同名方法的调用。

具体的这四个作用可以参考[内部类的作用](https://www.cnblogs.com/uu5666/p/8185061.html)



## 内部类为什么可以访问外部类的属性和方法

使用第一个局部内部类的示例来看，

```java
private class Inner {
  public void print() {
    outPrint();
    System.out.println("age" + age + count);
  }
}
```

内部类Inner访问了外部类的

* 私有方法:outPrint();
* 公有变量：count
* 私有变量：age

那么他是怎么访问到这些方法和变量的呢？

1. 查看编译出来的文件总共有三个：Out.class、Out\$Inner.class、Out$1.class

   

***使用命令：javap -c -l Out\$Inner***

Out$Inner.class内容如下

   ```java
class com.demo.li.basic.Out$Inner {
  final com.demo.li.basic.Out this$0;

  public void print();
    Code:
       0: aload_0
       1: getfield      #2                  // Field this$0:Lcom/demo/li/basic/Out;
       4: invokestatic  #4                  // Method com/demo/li/basic/Out.access$000:(Lcom/demo/li/basic/Out;)V
       7: getstatic     #5                  // Field java/lang/System.out:Ljava/io/PrintStream;
      10: new           #6                  // class java/lang/StringBuilder
      13: dup
      14: invokespecial #7                  // Method java/lang/StringBuilder."<init>":()V
      17: ldc           #8                  // String age
      19: invokevirtual #9                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      22: aload_0
      23: getfield      #2                  // Field this$0:Lcom/demo/li/basic/Out;
      26: invokestatic  #10                 // Method com/demo/li/basic/Out.access$100:(Lcom/demo/li/basic/Out;)I
      29: invokevirtual #11                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
      32: aload_0
      33: getfield      #2                  // Field this$0:Lcom/demo/li/basic/Out;
      36: getfield      #12                 // Field com/demo/li/basic/Out.count:I
      39: invokevirtual #11                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
      42: invokevirtual #13                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      45: invokevirtual #14                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      48: return
    LineNumberTable:
      line 19: 0
      line 20: 7
      line 21: 48
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0      49     0  this   Lcom/demo/li/basic/Out$Inner;

  com.demo.li.basic.Out$Inner(com.demo.li.basic.Out, com.demo.li.basic.Out$1);
    Code:
       0: aload_0
       1: aload_1
       2: invokespecial #1                  // Method "<init>":(Lcom/demo/li/basic/Out;)V
       5: return
    LineNumberTable:
      line 17: 0
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0       6     0  this   Lcom/demo/li/basic/Out$Inner;
          0       6     1    x0   Lcom/demo/li/basic/Out;
          0       6     2    x1   Lcom/demo/li/basic/Out$1;
}
   ```

   里面有两句比较重要的

```java
final com.demo.li.basic.Out this$0;

//访问out.print方法
1: getfield      #2                  // Field this$0:Lcom/demo/li/basic/Out;
4: invokestatic  #4                  // Method com/demo/li/basic/Out.access$000:(Lcom/demo/li/basic/Out;)V
 
//获取out.age值
23: getfield      #2                  // Field this$0:Lcom/demo/li/basic/Out;
26: invokestatic  #10                 // Method com/demo/li/basic/Out.access$100:(Lcom/demo/li/basic/Out;)I
  
//获取out.count值
33: getfield      #2                  // Field this$0:Lcom/demo/li/basic/Out;
36: getfield      #12                 // Field com/demo/li/basic/Out.count:I
```

所以：

1. 内部类会拥有外部类的一个引用***this$0***，可以通过它来访问非private属性
2. 内部类使用到的private字段，外部类会生成一个对应的access$开头的方法，

第二点可以从Out.class里面看到

***使用命令： javap -c -l Out***

```java
public class com.demo.li.basic.Out {
  public int count;

  public com.demo.li.basic.Out();
    Code:
       0: aload_0
       1: invokespecial #3                  // Method java/lang/Object."<init>":()V
       4: aload_0
       5: iconst_1
       6: putfield      #1                  // Field age:I
       9: aload_0
      10: iconst_2
      11: putfield      #4                  // Field count:I
      14: return
    LineNumberTable:
      line 7: 0
      line 9: 4
      line 11: 9
      line 17: 14
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0      15     0  this   Lcom/demo/li/basic/Out;

  public static void main(java.lang.String[]);
    Code:
       0: new           #8                  // class com/demo/li/basic/Out$Inner
       3: dup
       4: new           #9                  // class com/demo/li/basic/Out
       7: dup
       8: invokespecial #10                 // Method "<init>":()V
      11: dup
      12: invokevirtual #11                 // Method java/lang/Object.getClass:()Ljava/lang/Class;
      15: pop
      16: aconst_null
      17: invokespecial #12                 // Method com/demo/li/basic/Out$Inner."<init>":(Lcom/demo/li/basic/Out;Lcom/demo/li/basic/Out$1;)V
      20: astore_1
      21: aload_1
      22: invokevirtual #13                 // Method com/demo/li/basic/Out$Inner.print:()V
      25: return
    LineNumberTable:
      line 25: 0
      line 26: 21
      line 28: 25
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0      26     0  args   [Ljava/lang/String;
         21       5     1 inner   Lcom/demo/li/basic/Out$Inner;

  static void access$000(com.demo.li.basic.Out);
    Code:
       0: aload_0
       1: invokespecial #2                  // Method outPrint:()V
       4: return
    LineNumberTable:
      line 7: 0
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0       5     0    x0   Lcom/demo/li/basic/Out;

  static int access$100(com.demo.li.basic.Out);
    Code:
       0: aload_0
       1: getfield      #1                  // Field age:I
       4: ireturn
    LineNumberTable:
      line 7: 0
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0       5     0    x0   Lcom/demo/li/basic/Out;
}
```





> 上面的 access\$000的access$100都是为内部类访问而生成的static方法，一个访问outPrint方法，一个访问age字段。



所以内部类访问外部类的属性和方法的方式有下面两个

1. 传递到内部类的外部类引用 this$0
2. 为内部类生成的assess$开头的方法（编译器分析到内部类有使用这个变量才会生成）







## 参考文档
[几种内部类的介绍](https://www.cnblogs.com/dolphin0520/p/3811445.html)

[内部类为什么可以访问外部类的私有属性,重点acces方法](https://blog.csdn.net/ZytheMoon/article/details/85237366)