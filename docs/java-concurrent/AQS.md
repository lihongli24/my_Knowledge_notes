# AbstractQueuedSynchronizer(AQS)

[toc]

## 文章目的

### 背景

java.util.concurrent包里面提供了很多种锁一样的类，ReentrantLock（可再入锁）、CountDownLatch(共享式的计数阻塞器)、Semaphore(信号量)等等，他们实现的功能各异，但是功能的实现过程中，就有很大一部分逻辑可以抽出来通用，这就是AQS。

### 学习关键字

* 独占锁
* 共享锁
* 公平锁和非公平锁
* 自旋
* cas
* condition
* 同步队列
* 加锁和释放锁

> 写这篇文章的目的：因为AQS的实现有点复杂，看了别人的文章之后，当时能理解，但是过段时间就会忘掉，所以结合源码和网上的文章，总结一篇自己的笔记出来。方便日后理解和回忆。

## 使用示例和现成的实现

### 使用示例

```java
public class ReentrantLockTest {
    public static void main(String[] args) {
        //创建线程池
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(20);
        ExecutorService executorService = new ThreadPoolExecutor(10, 100,
            0L, TimeUnit.MILLISECONDS,blockingQueue);
        //创建10个线程，丢入线程池执行
        for (int i = 0; i < 10; i++){
            TryLockThread tryLockThread = new TryLockThread();
            tryLockThread.setName("" + i);
            executorService.submit(tryLockThread);
        }
    }
    //线程，执行时先获取锁，休眠1s
    static class TryLockThread extends Thread {
        private static ReentrantLock reentrantLock = new ReentrantLock();
        @Override
        public void run() {
            try {
                System.out.println("线程" + this.getName() + "开始获取锁=========");
                reentrantLock.lock();
                System.out.println("线程" + this.getName() + "成功获取锁!!!!!!!!!!!!!");
                Thread.sleep(1000L);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
                System.out.println("线程" + this.getName() + "释放锁ssssssssss");
            }
        }
    }
}
```
执行结果如下：
```java
线程0开始获取锁=========
线程1开始获取锁=========
线程0成功获取锁!!!!!!!!!!!!!
线程2开始获取锁=========
线程3开始获取锁=========
线程4开始获取锁=========
线程5开始获取锁=========
线程6开始获取锁=========
线程7开始获取锁=========
线程8开始获取锁=========
线程9开始获取锁=========
线程0释放锁ssssssssss
线程1成功获取锁!!!!!!!!!!!!!
线程1释放锁ssssssssss
线程2成功获取锁!!!!!!!!!!!!!
线程2释放锁ssssssssss
线程3成功获取锁!!!!!!!!!!!!!
线程3释放锁ssssssssss
线程4成功获取锁!!!!!!!!!!!!!
线程4释放锁ssssssssss
线程5成功获取锁!!!!!!!!!!!!!
线程6成功获取锁!!!!!!!!!!!!!
线程5释放锁ssssssssss
线程6释放锁ssssssssss
线程7成功获取锁!!!!!!!!!!!!!
线程7释放锁ssssssssss
线程8成功获取锁!!!!!!!!!!!!!
线程8释放锁ssssssssss
线程9成功获取锁!!!!!!!!!!!!!
线程9释放锁ssssssssss
```

上面的测试用例，表示只有一个线程释放所持有的锁之后，才会有另外一个线程能获取锁成功执行后续的代码。(代码路径：src/main/java/aqs/ReentrantLockTest.java)

用例里面获取锁和释放所的方法如下

```java
reentrantLock.lock();
reentrantLock.unlock();
```

### ReentrantLock的实现细节

#### ReentrantLock的鸟瞰图

```java
public class ReentrantLock implements Lock, java.io.Serializable {
    private static final long serialVersionUID = 7373984872572414699L;
    /** Synchronizer providing all implementation mechanics */
    private final Sync sync;
    abstract static class Sync extends AbstractQueuedSynchronizer {
    abstract void lock();
    public ReentrantLock() {
        sync = new NonfairSync();
    }
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
    public void lock() {
        sync.lock();
    }
  
	  public void unlock() {
        sync.release(1);
    }
}
```

具体代码被删除了不少，先从整体出发。



####ReentrantLock的获取锁和释放锁 

ReentrantLock里面持有一个Sync，这个就是我们的AQS的子类。这个Sync有两个实现类NonfairSync和FairSync，就是所谓的公平锁和非公平锁。

执行ReentrantLock的lock和unlock方法的时候，其实执行的就是具体的Sync的这两个方法。

```java
/**
     * Sync object for fair locks
     */
static final class FairSync extends Sync {
  private static final long serialVersionUID = -3000897897090466540L;

  final void lock() {
    acquire(1);
  }
}

/**
     * Sync object for non-fair locks
     */
static final class NonfairSync extends Sync {
  private static final long serialVersionUID = 7316153563782823691L;

  /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
  final void lock() {
    if (compareAndSetState(0, 1))
      setExclusiveOwnerThread(Thread.currentThread());
    else
      acquire(1);
  }
}
```

> 非公平锁和公平锁的区别就在于，非公平锁在执行lock的时候，会先尝试获取一次锁(CAS去占state字段)。如果成功，直接将当前线程设置成独占锁的持有者。
>
> 如果直接获取失败，那非公平锁和公平锁的操作是一样的，都是请求acquire(1)方法。这个方法有AQS提供。



ReentrantLock的unlock执行的是sync.release(1);，这个方法也是也是AQS的实现。ReentrantLock做的就是对AQS中的抽象方法做事先。

```java
AQS:
protected boolean tryAcquire(int arg) {
  throw new UnsupportedOperationException();
}

protected boolean tryRelease(int arg) {
  throw new UnsupportedOperationException();
}
```
#### 
```java
/**
* 公平锁
**/
static final class FairSync extends Sync {
  protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
      if (!hasQueuedPredecessors() &&
          compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(current);
        return true;
      }
    }
    else if (current == getExclusiveOwnerThread()) {
      int nextc = c + acquires;
      if (nextc < 0)
        throw new Error("Maximum lock count exceeded");
      setState(nextc);
      return true;
    }
    return false;
  }
}


/**
* 非公平锁
**/
static final class NonfairSync extends Sync {
  protected final boolean tryAcquire(int acquires) {
    return nonfairTryAcquire(acquires);
  }
}

/**
* 父类
**/
abstract static class Sync extends AbstractQueuedSynchronizer {
  final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
      if (compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(current);
        return true;
      }
    }
    else if (current == getExclusiveOwnerThread()) {
      int nextc = c + acquires;
      if (nextc < 0) // overflow
        throw new Error("Maximum lock count exceeded");
      setState(nextc);
      return true;
    }
    return false;
  }

  protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    if (Thread.currentThread() != getExclusiveOwnerThread())
      throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
      free = true;
      setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
  }
}
```

> 非公平锁直接使用了父类中的`nonfairTryAcquire`方法，两者比较就是公平锁在做cas抢占锁的之前会调用一个`hasQueuedPredecessors`方法判断，当前是否还有更早的节点在等待锁。其他两者是一样的。

加锁操作：

1. 如果当前state=0(竞争的资源空闲)，执行cas抢占锁
2. 如果抢占成功，执行setExclusiveOwnerThread，将当前线程设置成独占锁的所有者。
3. 如果state!=0,判断锁的持有者是不是当前线程，如果是，对state+acquires，这也就是可重入的意思。

释放锁操作(无需cas，因为只有持有锁才能执行)：

1. 先对state = state-releases
2. 如果state==0,执行setExclusiveOwnerThread(null);将锁持有者置空。
3. 如果state!=0,只做设值处理。


> 但是说了这么多，ReetrantLock只是对加锁和释放锁做了实现，那么多个线程同时请求获取锁的时候，到底是怎么被组织的呢？

## AQS源码分析

### lock

讲AQS,我们先捋一遍获取锁和释放锁的流程.

> AQS的核心是CLH。CLH锁也是一种基于链表的可扩展、高性能、公平的自旋锁，申请线程只在本地变量上自旋，它不断轮询前驱的状态，如果发现前驱释放了锁就结束自旋。先来看一下构成这个链的Node.

```java
static final class Node {
        //和下面的nextWaiter，一起用，当nextWaiter==SHARED这个Node的时候，标识当前处于共享
        static final Node SHARED = new Node();
  			//和上面区分，独占模式下的nextWaiter==null
        static final Node EXCLUSIVE = null;
				// 节点已取消，在特定步骤这些CANCELED状态的节点会被清除出同步队列
        static final int CANCELLED =  1;
  			// 标识本节点release的时候，会去对下一个节点执行unpark
        static final int SIGNAL    = -1;
  			//node处于Condition队列中
        static final int CONDITION = -2;
  			//这个可能需要分析完共享锁才能写，先空着
        static final int PROPAGATE = -3;
				// 状态，值取上面的CANCELLED、SIGNAL、CONDITION、PROPAGATE
        volatile int waitStatus;
				//同步队列，前一个节点
        volatile Node prev;

        //同步队列，后一个节点
        volatile Node next;
				//node代表的线程
        volatile Thread thread;

       //Condition队列的下个节点，如果是共享模式的话，就是SHARED这个Node
        Node nextWaiter;
}
```





### Condition

## AQS的其他实现

## 参考资料

* https://mp.weixin.qq.com/s/-swOI_4_cxP5BBSD9wd0lA
* 