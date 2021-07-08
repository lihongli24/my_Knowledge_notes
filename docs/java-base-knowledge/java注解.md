## java中的注解

java中的Annotation在定义的时候,一般会写如下代码

```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Slf4j {
	/**
	 * Sets the category of the constructed Logger. By default, it will use the type where the annotation is placed.
	 */
	String topic() default "";
}
```
其中会用到如下几个注解
![Annotation.jpg](https://user-gold-cdn.xitu.io/2019/7/11/16be1048796402c8?w=1240&h=893&f=jpeg&s=54331)

主要有如下：
### @Target

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Target {
    /**
     * Returns an array of the kinds of elements an annotation type
     * can be applied to.
     * @return an array of the kinds of elements an annotation type
     * can be applied to
     */
    ElementType[] value();
}
```
ElementType内容如下：

| 取值            | 描述             |
| --------------- | ---------------- |
| ANNOTATION_TYPE | 应用于注解类型   |
| CONSTRUCTOR     | 用于构造函数     |
| FIELD           | 用于字段或属性   |
| LOCAL_VARIABLE  | 局部变量上使用   |
| METHOD          | 方法上注释       |
| PACKAGE         | 用于包的声明上   |
| PARAMETER       | 作用于方法参数上 |
| TYPE            | 用于类的任何元素 |

==因为@Target是使用在Annotation上的，所以他的@Target(ElementType.ANNOTATION_TYPE)==

### @Retention

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Retention {
    /**
     * Returns the retention policy.
     * @return the retention policy
     */
    RetentionPolicy value();
}
```
RetentionPolicy取值如下：

| 取值    | 描述                                                         |
| ------- | ------------------------------------------------------------ |
| SOURCE  | 只作用于编译阶段，并且会被编译器丢弃                         |
| CLASS   | 在编译后会被放进class文件，但是在虚拟机运行期无效(==默认值==) |
| RUNTIME | 编译后会被放进class文件，在运行期间有效                      |

##### SOURCE:
```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Slf4j {
	/**
	 * Sets the category of the constructed Logger. By default, it will use the type where the annotation is placed.
	 */
	String topic() default "";
}
```
lombok.extern.slf4j下的@slf4j注解，是在编译器自动生成代码。所以在运行期间是没有作用的，所以可以写成RetentionPolicy.SOURCE,生成class文件可以将其丢弃。

##### SOURCE和CLASS的区别
对于服务端程序员来说，代码编译成class文件，后面就是去运行，处于运行期了，所以注解如果只是在class文件中，会被虚拟机忽略的话，和没进class有什么区别呢？？<br />
网上查的资料如下：
首先我们知道android从源码到apk文件的大体流程：
Java源码 —> Class文件 —> Dex文件 —> apk <br >
所以在安卓程序的开发过程中，写进Class文件的注解可以在class文件->app过程中起作用

### @Documented

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Documented {
}
```
默认情况下，生成javedoc文件的时候，Annotation是不包含在javadoc中的，加上@Documented之后，这个annotation可以出现在javadoc中


### @Inherited

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Inherited {
}
```

Inherited的中文意思就是 遗传的 <br>
所以@Inherited的作用是，如果父类中标注了由@Inherited修饰的注解时，子类可以从父类中==遗传==这个注解

### @Repeatable
```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Repeatable {
    /**
     * Indicates the <em>containing annotation type</em> for the
     * repeatable annotation type.
     * @return the containing annotation type
     */
    Class<? extends Annotation> value();
}

```
上述的几个注解都是在1.5的时候就有了，@Repeatable比较年轻，是在1.8的时候才引入的，作用是可以在一个位置可以使用多次@Repeatable修饰的注解
实例：
```java
@Repeatable(value = Repeats.class)
public @interface Repeat {
    String value();
}


public class RepeatDemo {

    @Repeat(value = "tag")
    @Repeat(value = "tag2")
    public void method() {
    }

}
```