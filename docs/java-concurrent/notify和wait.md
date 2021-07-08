## notify和wait

题目：启动三个线程，使他们有序的交替的打印0-100

最开始我想到的是
```
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest3 implements Runnable {

    private static final AtomicInteger atomicInteger = new AtomicInteger(0);
    private int i = 0;

    public static void main(String[] args) {
        ThreadTest3 t = new ThreadTest3();
        Thread t1 = new Thread(t);
        Thread t2 = new Thread(t);
        Thread t3 = new Thread(t);
        t1.setName("t1");
        t2.setName("t2");
        t3.setName("t3");
        t1.start();
        t2.start();
        t3.start();
    }


    @Override
    public void run() {
        while (true){
            if(atomicInteger.get() <100){
                System.out.println(Thread.currentThread().getName() + "_" + atomicInteger.getAndIncrement());
            }else {
                    break;
                }
        }
    }
}
```
结果出来的是
```
t1_0
t1_2
t1_3
t1_4
t1_5
t2_1
t2_7
t1_6
t1_9
t1_10
t1_11
t1_12
t1_13
t1_14
t1_15
t1_16
t1_17
t1_18
t2_8
......
```
一看就是既没有交替有没有有序

交替的话，需要一个线程执行完成之后，放弃cpu的资源，想到的就是wait方法，执行wait之前需要执行notify把其他的线程先唤醒

于是就有了第二个版本
```
public class ThreadTest7 extends Thread {

    private Object lock;
    private static int i;

    public ThreadTest7(Object lock) {
        this.lock = lock;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                notify();
                if (i < 100) {
                    System.out.println(Thread.currentThread().getName() + "_" + i);
                    i++;
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    break;
                }
            }

        }
    }

    public static void main(String[] args) {
        Object lock = new Object();
        Thread t1 = new ThreadTest7(lock);
        Thread t2 = new ThreadTest7(lock);
        Thread t3 = new ThreadTest7(lock);
        t1.setName("t1");
        t2.setName("t2");
        t3.setName("t3");
        t1.start();
        t2.start();
        t3.start();
    }
}

```
输出结果为：交替是交替了，但是没有打完
```
t1_0
t2_1
t3_2
没有然后了
```


---


查了网上的资料，
```
public class ThreadTest4 implements Runnable{

    int i = 1;

    public static void main(String[] args) {
        ThreadTest4 t = new ThreadTest4();
        Thread t1 = new Thread(t);
        Thread t2 = new Thread(t);

        t1.setName("线程1");
        t2.setName("线程2");

        t1.start();t2.start();
    }

    public void run() {
        while (true) {
            synchronized (this) {
                // 先唤醒另外一个线程
                notify();
                if (i <= 100) {
                    System.out.println(this.getClass().getName() + "_" +  this.hashCode());
                    System.out.println(Thread.currentThread().getName() + ":"+ i);
                    i++;
                    try {
                        // 打印完之后，释放资源，等待下次被唤醒
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    break;
                }
            }
        }
    }

}
```
结果果然可以交替有序的把数字打印出来了。
> Runnable和Thread创建线程的区别<br>
> Runnable r = new Runnable();<br>
> Thread t1 = new Thread(r);<br>
> Thread t2 = new Thread(r);<br>
> t1和t2会共享到r中的属性，比如上例中的i，以及this也是同一个Runnable t.

### notify、notifyAll 和 wait
这三个方法都是Object的方法，
| 方法      | 注释                                                         |
| --------- | ------------------------------------------------------------ |
| notify    | Wakes up a single thread that is waiting on this object's monitor. If any threads are waiting on this object, one of them is chosen to be awakened |
| notifyAll | Wakes up all threads that are waiting on this object's monitor. A thread waits on an object's monitor by calling one of the {@code wait} methods. |
| wait      | Causes the current thread to wait until another thread invokes the {@link java.lang.Object#notify()} method or the{@link java.lang.Object#notifyAll()} method for this object. |

notify和notifyAll起作用的前提是，他们需要获取`同一个object实例的monitor`（synchronized的实现原理，synchronized修饰的代码块编译完了之后，其实是monitorenter和monitorexit两个指令包的，要不然无法唤醒其他的线程，就像我的ThreadTest7。

所以这三个方法`必须放在同步代码块中`，当线程执行wait()时，会把当前的锁释放，然后让出CPU，进入等待状态。

按照上面的概念，所以如果要使用Thread不用Runnable的话，可以使用下面的方式
```
public class ThreadTest6 extends Thread {

    private static Object lock = new Object();

    private static int i;


    @Override
    public void run() {
        while (true) {

            synchronized (lock) {
                lock.notify();
                if (i < 100) {
                    System.out.println(Thread.currentThread().getName() + "_" + i);
                    i++;
                }
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static void main(String[] args) {
        Thread t1 = new ThreadTest6();
        Thread t2 = new ThreadTest6();
        Thread t3 = new ThreadTest6();
        t1.setName("t1");
        t2.setName("t2");
        t3.setName("t3");
        t1.start();
        t2.start();
        t3.start();
    }
}

```