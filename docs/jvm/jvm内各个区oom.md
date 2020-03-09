# jvm内各个区的oom

![](https://tva1.sinaimg.cn/large/00831rSTgy1gcnqxy8sejj30kr0d2abs.jpg)

## 程序计数器(线程私有)
唯一不会发生oom的地方

## 方法区(线程公用)
方法区存放的是class的信息，类信息、方法信息、字段信息等，方法区出现oom的可能情况有
1. 运行时动态生成了大量的类，填满了方法区。比如使用cglib动态生成类

> 方法区的回收效率会比堆低很多(因为条件比较苛刻)
> 方法区类信息被标记为无用的类需要满足下面三个条件：(满足下面的条件才有可能会被回收)
> 1. Java堆中不存在该类的任何实例对象；
> 2. 加载该类的类加载器已经被回收 
> 3. 该类对应的java.lang.Class对象不在任何地方被引用，且无法在任何地方通过反射访问该类的方法。

***永久代(Perm)和元空间(Metaspace)***
两者都是方法区的实现，***方法区是定义，永久代和元空间是实现***

有人说：HotSpot之所以用永久带来实现方法区是因为这样可以不必专门为方法区编写一套内存管理的代码。

在1.7之前，可以使用如下参数来调节方法区的大小

-XX:PermSize
方法区初始大小
-XX:MaxPermSize
方法区最大大小
超过这个值将会抛出OutOfMemoryError异常:java.lang.OutOfMemoryError: PermGen


在1.8中，可以使用如下参数来调节方法区的大小

-XX:MetaspaceSize
元空间初始大小
-XX:MaxMetaspaceSize
元空间最大大小
超过这个值将会抛出OutOfMemoryError异常:java.lang.OutOfMemoryError: Metadata space
在jdk1.7中抛出的异常是这样:java.lang.OutOfMemoryError: PermGen




## java虚拟机栈和本地方法占(线程私有)
***StrackOverflowError***: 一个虚拟机栈中，一次方法调用就是往该线程的栈中压入一个栈侦，如果一个线程往栈中压入太多的栈侦的话，可能会出现超出虚拟机所允许的最大深度，这个时候会抛出StrackOverflowError
***OOM***： 上面的情况，可以使用允许动态拓展虚拟机栈的操作解决，但是当拓展的时候无法申请到足够的内存的时候，就是抛出OOM

## 堆
最常见出现OOM的地方是堆中
1. 应用程序中bug，导致对应的应用被保存了。比如：将new 出来的对象都丢入一个static List里面。因为static List属于方法区，所以只要它不被清除，list里面关联的对象都不会被回收。
2. 应用程序过度使用 finalizer。finalizer 对象不能被 GC 立刻回收。finalizer 由结束队列服务的守护线程调用，有时 finalizer 线程的处理能力无法跟上结束队列的增长***java的finalizer方法，会在标记回收的过程中让对象有一次逃脱的机会，但是下次再回收的时候还是会被回收***
3. 堆的大小分配太小，或者需要存入堆中的对象实在是太大(比如一个超级大的String之类的对象)。

***解决方案***
使用 -Xmx 增加堆大小
修复应用程序中的内存泄漏

> Minor GC:当新创建对象，内存空间不够的时候，就会执行这个垃圾回收。由于执行最频繁，因此一般采用复制回收机制。
> Major GC:清理年老代的内存，这里一般采用的是标记清除+标记整理机制。
> Full GC:有的说与Major GC差不多，有的说相当于执行minor+major回收，那么我们暂且可以认为Full GC就是全面的垃圾回收吧。

## 直接内存
在1.4加入Nio后，引入了Channel和Buffer的I/O方式，可使用native函数库直接分配堆外内存， 然后通过DirectByteBuffer对象作为这块内存的引用进行操作，这样做可使Java堆与native堆免于来回复制， 提高了性能。 开发中我们可以使用java.nio.DirectByteBuffer对象进行堆外内存的管理和使用， 它会在对象创建的时候就分配堆外内存。

1. 接内存申请空间耗费更高的性能，当频繁申请到一定量时尤为明显
2. 直接内存IO读写的性能要优于普通的堆内存，在多次读写操作的情况下差异明显

> 从数据流的角度，来看
> 非直接内存作用链:
> 本地IO –>直接内存–>非直接内存–>直接内存–>本地IO
> 直接内存作用链:
> 本地IO–>直接内存–>本地IO

***由于堆外内存并不直接控制于JVM，因此只能等到full GC的时候才能垃圾回收！（direct buffer归属的的JAVA对象是在堆上且能够被GC回收的，一旦它被回收，JVM将释放direct buffer的堆外空间。前提是没有关闭DisableExplicitGC）***

解决办法：
1. 合理地进行Full GC的执行***不知道怎么进行合理的full gc ***
2. 设定一个系统实际可达的-XX:MaxDirectMemorySize的值，这样，当直接内存使用超过该值得时候就会触发GC，从而避免内存溢出问题.(这样也会因此频繁的GC导致系统执行变慢，应当斟酌设置)

[直接内存知识点](https://blog.csdn.net/leaf_0303/article/details/78961936)
[回收直接内存的方法](https://www.cnblogs.com/duanxz/p/6089485.html)