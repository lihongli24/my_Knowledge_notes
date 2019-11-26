# jdk的检测工具

## jps(JVM Process Status) 查看java进程
| 命令   | 作用                                                     |
| :----- | -------------------------------------------------------- |
| jps -l | 输出主类的全名，如果进程执行的是 Jar 包，输出 Jar 路径。 |
| jps -q | 只输出进程的本地虚拟机唯一 ID。|
| jps -v| 输出虚拟机进程启动时 JVM 参数。|
| jps -m | 输出传递给 Java 进程 main() 函数的参数。|

## jstat(JVM Statistics Monitoring Tool)可用来打印目标 Java 进程的性能数据
```java
jstat -options
-class //-class将打印类加载相关的数据
-compiler //编译相关数据
-gc
-gccapacity
-gccause
-gcmetacapacity
-gcnew
-gcnewcapacity
-gcold
-gcoldcapacity
-gcutil
-printcompilation
```

> 在这些子命令中，-class将打印类加载相关的数据，-compiler和-printcompilation将打印即时编译相关的数据。剩下的都是以-gc为前缀的子命令，它们将打印垃圾回收相关的数据。

默认情况下，jstat只会打印一次性能数据。我们可以将它配置为每隔一段时间打印一次，直至目标 Java 进程终止，或者达到我们所配置的最大打印次数。具体示例如下所示：



```java
jstat -gc 871 1s 4
 S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT
30528.0 30528.0 12018.7  0.0   244288.0 187604.7  610516.0   210807.0  300836.0 272444.7 39284.0 33718.0    590   81.140  40     46.671  127.810
30528.0 30528.0 12018.7  0.0   244288.0 187604.7  610516.0   210807.0  300836.0 272444.7 39284.0 33718.0    590   81.140  40     46.671  127.810
30528.0 30528.0 12018.7  0.0   244288.0 187609.2  610516.0   210807.0  300836.0 272444.7 39284.0 33718.0    590   81.140  40     46.671  127.810
30528.0 30528.0 12018.7  0.0   244288.0 187609.2  610516.0   210807.0  300836.0 272444.7 39284.0 33718.0    590   81.140  40     46.671  127.810
```

- **S0C：**第一个幸存区的大小
- **S1C：**第二个幸存区的大小
- **S0U：**第一个幸存区的使用大小
- **S1U：**第二个幸存区的使用大小
- **EC：**伊甸园区的大小
- **EU：**伊甸园区的使用大小
- **OC：**老年代大小
- **OU：**老年代使用大小
- **MC：**方法区大小
- **MU：**方法区使用大小
- **CCSC:**压缩类空间大小
- **CCSU:**压缩类空间使用大小
- **YGC：**年轻代垃圾回收次数
- **YGCT：**年轻代垃圾回收消耗时间
- **FGC：**老年代垃圾回收次数
- **FGCT：**老年代垃圾回收消耗时间
- **GCT：**垃圾回收消耗总时间

**jstat还可以用来判断是否出现内存泄漏**  。在长时间运行的 Java 程序中，

1. 我们可以运行jstat命令连续获取多行性能数据，并取这几行数据中 OU 列（即已占用的老年代内存）的最小值。
2. 然后，我们每隔一段较长的时间重复一次上述操作，来获得多组 OU 最小值。
3. 如果这些值呈上涨趋势，则说明该 Java 程序的老年代内存已使用量在不断上涨，这意味着无法回收的对象在不断增加，因此很有可能存在内存泄漏。

## jmap(查看或者生成虚拟机堆快照)

我们通常会利用jmap -dump:live,format=b,file=filename.bin命令，将堆中所有存活对象导出至一个文件之中。



## jinfo(用来查看目标 Java 进程的参数)

jinfo命令（帮助文档）可用来查看目标 Java 进程的参数，如传递给 Java 虚拟机的-X（即输出中的 jvm_args）、-XX参数（即输出中的 VM Flags），以及可在 Java 层面通过System.getProperty获取的-D参数（即输出中的 System Properties）。



## jstack(打印线程栈轨迹，以及持有的锁)

jstack命令（帮助文档）可以用来打印目标 Java 进程中各个线程的栈轨迹，以及这些线程所持有的锁。jstack的其中一个应用场景便是死锁检测



### jstack死锁的排查

**下面是一个线程死锁的代码。我们下面会通过 `jstack` 命令进行死锁检查，输出死锁信息，找到发生死锁的线程。**

```java
public class DeadLockDemo {
    private static Object resource1 = new Object();//资源 1
    private static Object resource2 = new Object();//资源 2

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (resource1) {
                System.out.println(Thread.currentThread() + "get resource1");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread() + "waiting get resource2");
                synchronized (resource2) {
                    System.out.println(Thread.currentThread() + "get resource2");
                }
            }
        }, "线程 1").start();

        new Thread(() -> {
            synchronized (resource2) {
                System.out.println(Thread.currentThread() + "get resource2");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread() + "waiting get resource1");
                synchronized (resource1) {
                    System.out.println(Thread.currentThread() + "get resource1");
                }
            }
        }, "线程 2").start();
    }
}
```

Output

```
Thread[线程 1,5,main]get resource1
Thread[线程 2,5,main]get resource2
Thread[线程 1,5,main]waiting get resource2
Thread[线程 2,5,main]waiting get resource1
```

线程 A 通过 synchronized (resource1) 获得 resource1 的监视器锁，然后通过`Thread.sleep(1000);`让线程 A 休眠 1s 为的是让线程 B 得到执行然后获取到 resource2 的监视器锁。线程 A 和线程 B 休眠结束了都开始企图请求获取对方的资源，然后这两个线程就会陷入互相等待的状态，这也就产生了死锁。

1. 先使用jps查询到进程Id

```java
jps
871
1560 Launcher
5774 Jps
5535 DeadLockDemo
```

2. 使用jstack ,发现存在死锁

```java
jstack 5535


 Found one Java-level deadlock:
=============================
"线程 2":
  waiting to lock monitor 0x00007f841a04a4a8 (object 0x0000000740000fd0, a java.lang.Object),
  which is held by "线程 1"
"线程 1":
  waiting to lock monitor 0x00007f841a047c18 (object 0x0000000740000fe0, a java.lang.Object),
  which is held by "线程 2"

Java stack information for the threads listed above:
===================================================
"线程 2":
	at DeadLockDemo.lambda$main$1(DeadLockDemo.java:31)
	- waiting to lock <0x0000000740000fd0> (a java.lang.Object)
	- locked <0x0000000740000fe0> (a java.lang.Object)
	at DeadLockDemo$$Lambda$2/531885035.run(Unknown Source)
	at java.lang.Thread.run(Thread.java:745)
"线程 1":
	at DeadLockDemo.lambda$main$0(DeadLockDemo.java:16)
	- waiting to lock <0x0000000740000fe0> (a java.lang.Object)
	- locked <0x0000000740000fd0> (a java.lang.Object)
	at DeadLockDemo$$Lambda$1/791452441.run(Unknown Source)
	at java.lang.Thread.run(Thread.java:745)

```

