# java 内存模型



## 内存模型是为了解决什么问题

##### 主内存和cpu缓存一致性

- `cpu高速缓存的出现` cpu发展速度很快，但是内存memory的发展却没那么快，原来的cpu直接和内存做交互的模式，会导致cpu会浪费很多时间在等待上。所以，在cpu中增加了高速缓存(多级)，`在运行时，会从主内存中获取数据放入高速缓存中做一份数据拷贝，等cpu处理完成之后，再刷新到主内存中`
- `多核cpu`,如果是单核cpu的话，即使有主内存和cpu缓存的区别，但是是因为单核cpu，所以使用到的高速缓存和主内存中的数据是同一份，也不会造成问题。但是现在的cpu都是多核的，那就存在`一份主内存`和`多份cpu高速缓存`,这样就会出现对同一份数据，多个cpu缓存中的数据不一致的情况

##### 处理器优化

- 是为了使处理器内部的运算单元能够尽量的被充分利用，处理器可能会对输入代码进行乱序执行处理。这就是处理器优化。

##### 指令重排

- 除了现在很多流行的处理器会对代码进行优化乱序处理，很多编程语言的编译器也会有类似的优化，比如Java虚拟机的即时编译器（JIT）也会做指令重排。

#### 并发开发需要满足的特性：

1. `原子性`： 指在一个操作中就是cpu不可以在中途暂停然后再调度，既不被中断操作，要不执行完成，要不就不执行。
2. `可见性`: 指当多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即看得到修改的值
3. `有序性`: 程序执行的顺序按照代码的先后顺序执行。

> 以上的理论上是为了让程序执行更快的进步和升级手段，也为并发开发带来了一些麻烦
> \1. `缓存一致性问题`就是`可见性问题`
> \2. 指令重排和处理器优化可能导致原子性问题和有序性问题

##### 内存模型

`为了保证共享内存的正确性（可见性、有序性、原子性），内存模型定义了共享内存系统中多线程程序读写操作行为的规范。`通过这些规则来规范对内存的读写操作，从而保证指令执行的正确性。它与处理器有关、与缓存有关、与并发有关、与编译器也有关。他解决了CPU多级缓存、处理器优化、指令重排等导致的内存访问问题，保证了并发场景下的一致性、原子性和有序性。

## java 内存模型

java内存模型是符合内存模型的一种规范

```
JMM是一种规范，目的是解决由于多线程通过共享内存进行通信时，存在的本地内存数据不一致、编译器会对代码指令重排序、处理器会对代码乱序执行等带来的问题。
```

- 主内存： 计算机的物理内存 memory
- 工作内存： 每一条线程都拥有自己的工作内存，用于保存用到的变量的主内存复本拷贝。每个线程对数据的操作，都是在工作内存中进行，不会读写主内存。

> ```
> Java内存模型，除了定义了一套规范，还提供了一系列原语，封装了底层实现后，供开发者直接使用。包括synchronized和volatile等
> ```

#### synchronized

- 当synchronized作用于普通方法是，锁对象是this；
- 当synchronized作用于静态方法是，锁对象是当前类的Class对象；
- 当synchronized作用于代码块时，锁对象是synchronized(obj)中的这个obj。

对于同步方法，JVM采用`ACC_SYNCHRONIZED`标记符来实现同步。 对于同步代码块。JVM采用`monitorenter`、`monitorexit`两个指令来实现同步。

1. 实现原子性：

   通过`monitorenter`和`monitorexit`指令，可以保证被`synchronized`修饰的代码在同一时间只能被一个线程访问，在锁未释放之前，无法被其他线程访问到。因此，在Java中可以使用`synchronized`来保证方法和代码块内的操作是原子性的。当`monitorenter`后如果线程的`时间片到了`，因为锁没有释放，所以别的线程还是不会中途插入执行当前代码，等重新轮到时间片，利用`synchronized`的可重入性，本线程还是可以继续执行，实现原子性。

2. 可见性(`仅对synchronized锁定对象保证可见性`)

   被synchronized修饰的代码，在开始执行时会加锁，执行完成后会进行解锁。而为了保证可见性，有一条规则是这样的：`对一个变量解锁之前，必须先把此变量同步回主存中。这样解锁后，后续线程就可以访问到被修改后的值。`
   所以，synchronized关键字锁住的对象，其值是具有可见性的

3. 有序性

   重排序的前提是保证单线程执行结果不能被改变，`synchronized`修饰的方法或者代码块在同一时间段内只有一个线程能访问，所以可以保证执行结果不变。

#### volatile

[讲的极其详细](https://www.hollischuang.com/archives/2673)

1. 有序性

   volatile是通过内存屏障来来禁止指令重排的，达到有序性

2. 可见性

   Java中的volatile关键字提供了一个功能，那就是被其修饰的变量在被修改后可以立即同步到主内存，被其修饰的变量在每次是用之前都从主内存刷新。

3. 原子性

   `volatile无法保证原子性`

## 参考链接

1. https://www.hollischuang.com/archives/2550
2. https://www.hollischuang.com/archives/2637
3. https://www.hollischuang.com/archives/2673