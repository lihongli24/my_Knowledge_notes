# JMM 学习笔记
[toc]

## 缓存的一致性保证
硬件上有缓存一致性协议支持
intel : MESI协议。其他的cpu开发上还有 MOSI等协议
[mesi协议介绍] https://www.cnblogs.com/z00377750/p/9180644.html
在硬件层面保证，同一个缓存行中数据，在多个cpu中保持一致.

现代CPU的数据一致性实现 = 缓存锁(MESI ...) + 总线锁

使用总线锁的情况：有些没法缓存，或者跨越了多个缓存行的数据，还是使用了总线锁的方式保证数据的一致性。
![](https://tva1.sinaimg.cn/large/0081Kckwgy1gkh1tkcnjjj31co0ikb0f.jpg)


### 缓存行概念
为了增加访问效率、以及根据计算机的局限性原理，一次载入数据时，会将相邻的数据也进行载入。
cpu载入数据的基本单位叫做缓存行，cache line
目前：cache line的大小为 64字节

### cache line的伪共享
![](https://tva1.sinaimg.cn/large/0081Kckwgy1gkh1igl1aqj30to0pg4q0.jpg)
如上图中，x和y如果在同一个缓存行中，cpu1 频繁更新缓存行中的x，cpu2频繁更新缓存行中的y,两个cpu需要不断的同步这个缓存行，其实不相关的数据，因为被放在同一个缓存行中，导致相互影响。这种情况叫做cache line的伪共享问题。

#### 伪共享的解决方法
disruptor框架中的代码如下：
```java
public final class RingBuffer<E> extends RingBufferFields<E> implements Cursored, EventSequencer<E>, EventSink<E>
{
    public static final long INITIAL_CURSOR_VALUE = Sequence.INITIAL_VALUE;
    protected long p1, p2, p3, p4, p5, p6, p7;

```

因为一个缓存行的大小为64个字节，一个long的大小为8个字节。
如果想让自己的long数据不和别的数据在同一个缓存行里面，这里使用了放7个多余的long类型数据，这样就能让当前对象独占一个缓存行，不会出现伪共享cache line的情况。

1. jdk1.8之前：增加多余字段进行缓存行对齐
2. jdk1.8开始：@Contended注解，来进行缓存行对齐。让jvm来排除不同系统见的缓存行大小的差异。

## 乱序问题

CPU为了提高指令执行效率，会在一条指令执行过程中（比如去内存读数据（慢100倍）），去同时执行另一条指令，前提是，两条指令没有依赖关系

```java
private static int x = 0, y = 0;
    private static int a = 0, b =0;

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        for(;;) {
            i++;
            x = 0; y = 0;
            a = 0; b = 0;
            Thread one = new Thread(new Runnable() {
                public void run() {
                    //由于线程one先启动，下面这句话让它等一等线程two. 读着可根据自己电脑的实际性能适当调整等待时间.
                    //shortWait(100000);
                    a = 1;
                    x = b;
                }
            });

            Thread other = new Thread(new Runnable() {
                public void run() {
                    b = 1;
                    y = a;
                }
            });
            one.start();other.start();
            one.join();other.join();
            String result = "第" + i + "次 (" + x + "," + y + "）";
            if(x == 0 && y == 0) {
                System.err.println(result);
                break;
            } else {
                //System.out.println(result);
            }
        }
    }
```
上述代码，只要不发生指令的重排序，就不会出现x=0、y=0的情况。
但是执行之后是有时候出现这种情况的，所以存在指令重排序。