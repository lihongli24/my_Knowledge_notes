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

### ReentrantLock使用示例

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
##### 获取锁
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

#####  释放锁
```java
/**
* 父类
**/
abstract static class Sync extends AbstractQueuedSynchronizer {
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
释放锁操作(无需cas，因为只有持有锁才能执行)：

1. 先对state = state-releases
2. 如果state==0,执行setExclusiveOwnerThread(null);将锁持有者置空。
3. 如果state!=0,只做设值处理。


> 但是说了这么多，ReetrantLock只是对加锁和释放锁做了实现，那么多个线程同时请求获取锁的时候，到底是怎么被组织的呢？

### Semaphore使用示例

```java
public class SemaphoreTest {
    private static final Semaphore semaphore = new Semaphore(3);
    public static void main(String[] args) {
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(20);
        ExecutorService executorService = new ThreadPoolExecutor(10, 100,
            0L, TimeUnit.MILLISECONDS, blockingQueue);
        //创建10个线程，丢入线程池执行
        for (int i = 0; i < 10; i++) {
            AcquireThread acquireThread = new AcquireThread();
            acquireThread.setName("" + i);
            executorService.submit(acquireThread);
        }
    }
    static class AcquireThread extends Thread {
        @Override
        public void run() {
            System.out.println("线程" + this.getName() + "开始执行。。。");
            try {
                semaphore.acquire();
                System.out.println("线程" + this.getName() + "获取到资源开始运行！！！！！");
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException("线程" + this.getName() + "运行失败");
            } finally {
                semaphore.release();
                System.out.println("线程" + this.getName() + "已释放资源ssssss");
            }
        }
    }
}
```

运行结果:

```java
线程0开始执行。。。
线程1开始执行。。。
线程1获取到资源开始运行！！！！！
线程0获取到资源开始运行！！！！！
线程2开始执行。。。
线程2获取到资源开始运行！！！！！
线程3开始执行。。。
线程4开始执行。。。
线程5开始执行。。。
线程6开始执行。。。
线程7开始执行。。。
线程8开始执行。。。
线程9开始执行。。。
线程1已释放资源ssssss
线程2已释放资源ssssss
线程4获取到资源开始运行！！！！！
线程0已释放资源ssssss
线程5获取到资源开始运行！！！！！
线程3获取到资源开始运行！！！！！
线程5已释放资源ssssss
线程6获取到资源开始运行！！！！！
线程4已释放资源ssssss
线程3已释放资源ssssss
线程8获取到资源开始运行！！！！！
线程7获取到资源开始运行！！！！！
线程8已释放资源ssssss
线程9获取到资源开始运行！！！！！
线程6已释放资源ssssss
线程7已释放资源ssssss
线程9已释放资源ssssss
```

可以看出来，同一时刻会有三个线程同时竞争到资源

#### Semaphore源码分析

> 首先,Semaphore中也是维护一个实现了AQS的Sync，也分公平和非公平两种。所以直接看两个锁的tryAcquireShared和tryReleaseShared

##### Semaphore加锁

```java
abstract static class Sync extends AbstractQueuedSynchronizer {
  private static final long serialVersionUID = 1192457210091910933L;
  Sync(int permits) {
    setState(permits);
  }
  final int getPermits() {
    return getState();
  }
  final int nonfairTryAcquireShared(int acquires) {
    for (;;) {
      //获取当前state,减去当前的申请量(acquires)，
      int available = getState();
      int remaining = available - acquires;
      //如果资源减完了，返回
      //如果还有资源，cas占用资源，返回
      if (remaining < 0 ||
          compareAndSetState(available, remaining))
        return remaining;
    }
  }
}


//非公平锁，直接使用sync的获取锁方法
static final class NonfairSync extends Sync {
  private static final long serialVersionUID = -2694183684443567898L;

  NonfairSync(int permits) {
    super(permits);
  }

  protected int tryAcquireShared(int acquires) {
    return nonfairTryAcquireShared(acquires);
  }
}

//公平锁
static final class FairSync extends Sync {
  private static final long serialVersionUID = 2014338818796000944L;

  FairSync(int permits) {
    super(permits);
  }

  protected int tryAcquireShared(int acquires) {
    for (;;) {
      //判断是否有当前队列里是不是有比本节点更早的节点，如果有直接申请失败
      if (hasQueuedPredecessors())
        return -1;
      int available = getState();
      int remaining = available - acquires;
      if (remaining < 0 ||
          compareAndSetState(available, remaining))
        return remaining;
    }
  }
}
```

> 和reentrantLock的区别就是state代表资源的数量，允许多个线程来获取，只要还有剩余就能获取成功。这也是共享锁和排它锁的区别。

##### Semaphore的释放

```java
abstract static class Sync extends AbstractQueuedSynchronizer {
  private static final long serialVersionUID = 1192457210091910933L;

  //释放锁，
  protected final boolean tryReleaseShared(int releases) {
    for (;;) {
      //获取当前的state,把需要释放的资源数加回去，通过cas设置回去。
      int current = getState();
      int next = current + releases;
      if (next < current) // overflow
        throw new Error("Maximum permit count exceeded");
      if (compareAndSetState(current, next))
        return true;
    }
  }
}
```



实际使用过程中，调用的方法是下面两个,调用的就是sync的方法，也就是AQS里面的实现

```java

public void acquire(int permits) throws InterruptedException {
  if (permits < 0) throw new IllegalArgumentException();
  sync.acquireSharedInterruptibly(permits);
}


 public void release(int permits) {
   if (permits < 0) throw new IllegalArgumentException();
   sync.releaseShared(permits);
 }
```









## AQS源码分析

### 独占锁加锁

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

按照ReentrantLock的lock方法里面的逻辑，实际申请锁的逻辑就是调用sync.acquire(1);而这个acquire就是AQS的实现。

```java
public final void acquire(int arg) {
  if (!tryAcquire(arg) &&
      acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
    selfInterrupt();
}
```

拆解出来的逻辑就是

1. tryAcquire 调用实现类的tryAcquire方法，尝试获取锁
2. addWaiter 创建一个Node节点，加入到AQS维护的同步队列中
3. acquireQueued 使用自旋的方式，如果当前节点的前置节点是head,尝试获取锁，否则挂起当前线程`LockSupport.park(this);`
4. 如果第3步的acquireQueued的执行逻辑中发现线程状态为中断，则标记当前线程为中断。---`这一点还是没有弄明白`

#### tryAcquire（尝试获取锁）

该方法在AQS类中没有做实现，具体的实现细节在ReentrantLock这种实现类的Sync中会做实现。

####addWaiter（根据当前线程创建Node,加入同步队列）

```java

private Node addWaiter(Node mode) {
  //将当前线程封装到Node结构体中
  Node node = new Node(Thread.currentThread(), mode);
  // Try the fast path of enq; backup to full enq on failure
  Node pred = tail;
  //如果队列中已经存在节点了，快速尝试加入队列
  if (pred != null) {
    node.prev = pred;
    if (compareAndSetTail(pred, node)) {
      pred.next = node;
      return node;
    }
  }
  enq(node);
  return node;
}
```

1. 将当前线程封装到一个Node结构体中，mode在共享锁时是SHARD的静态Node,独占锁的时候是null

2. 如果当前存在tail节点，尝试快速添加当前节点到tail,将原来的tail设置成当前的前置节点

3. 否则执行enq(node)

##### enq(node)(将node加入队列)

```java
private Node enq(final Node node) {
  for (;;) {
    Node t = tail;
    //如果当前tail节点为空，说明队列肯定是空的，初始化队列（将队列的head和tail执行一个空的Node）
    if (t == null) { 
      if (compareAndSetHead(new Node()))
        tail = head;
    } else {
      //如果存在队列了，将新节点加入队尾
      node.prev = t;
      if (compareAndSetTail(t, node)) {
        t.next = node;
        return t;
      }
    }
  }
}
```

1. 使用自旋的方式
2. 如果当前队列中还没有tail，说明该队列还是空的。往head中塞一个空的Node.把tail也执行这个 空节点，结束本次自旋
3. 因为是for循环，第二次重新进来的时候，tail肯定有值了，所以把当前的Node放到tail位置，把原来的tail设置成当前node的前置节点。
4. 至此，代表本线程的node已经成功加入到了这条同步队列中了。

#### acquireQueued(入队之后，尝试获取锁)

```java
final boolean acquireQueued(final Node node, int arg) {
  boolean failed = true;
  try {
    boolean interrupted = false;
    for (;;) {
      //获取前置节点
      final Node p = node.predecessor();
      //如果前置节点是头结点，尝试获取锁。---因为head节点标识正在执行的线程
      if (p == head && tryAcquire(arg)) {
        setHead(node);
        p.next = null; // help GC
        failed = false;
        return interrupted;
      }
      if (shouldParkAfterFailedAcquire(p, node) &&
          parkAndCheckInterrupt())
        interrupted = true;
    }
  } finally {
    if (failed)
      cancelAcquire(node);
  }
}
```

1. 又是一次开启自旋
2. 获取当前节点的前置节点p
3. 如果p是head节点的话，尝试获取锁。（因为头结点是空节点或者正在持有锁的线程节点--当前节点这个时候尝试获取一把，可能正好锁已经闲置了，这样就能省去一次挂起和唤醒）
4. 如果第3步获取成功，则将head指向当前节点，把原先的head出队列、
5. 如果第3步获取失败，shouldParkAfterFailedAcquire判断是否需要挂起，如不挂起，则继续自旋
6. 如果判断为需要挂起，执行parkAndCheckInterrupt进行挂起，并且在挂起唤醒的时候，返回线程的中断状态。

##### shouldParkAfterFailedAcquire（判断是否能挂起当前线程）

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
  //获取前置节点的waitStatus
  int ws = pred.waitStatus;
  //如果前置节点的状态是SIGNAL，说明他会在释放锁的时候，主动唤醒下个节点，所以当前节点可以挂起
  if (ws == Node.SIGNAL)
    return true;
  //如果ws>0,表示已cancel,遍历清理该节点之前的连续的cancel节点，结束本次循环---因为本方法的外面是个循环
  if (ws > 0) {
    do {
      node.prev = pred = pred.prev;
    } while (pred.waitStatus > 0);
    pred.next = node;
  } else {
    //通过cas将前置节点的状态设置成SIGNAL，让他释放锁的时候来唤醒本线程。结束本次循环---因为本方法的外面是个循环
    compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
  }
  return false;
}
```

##### parkAndCheckInterrupt

```java
private final boolean parkAndCheckInterrupt() {
  //挂起当前线程
  LockSupport.park(this);
  //走到这边，说明上面的挂起已经结束了，返回当前的线程中断状态
  return Thread.interrupted();
}
```

到这里，如果线程有别唤起了，理论上是因为前置节点是head,而且他已经在执行释放锁了，所以继续`acquireQueued`的自旋，尝试锁。大不了不行就继续挂起呗！

### 独占锁释放

在ReentrantLock的源码分析的时候，发现unlock的时候调用的是sync的release(1)方法。这个release方法又是AQS的模板实现。

#### release

```java
public final boolean release(int arg) {
  //使用子类的实现，进行锁的释放。参见ReentrantLock的Sync
  if (tryRelease(arg)) {
    //如果释放成功，获取同步队列头结点，调用unparkSuccessor，唤醒后继节点
    Node h = head;
    if (h != null && h.waitStatus != 0)
      unparkSuccessor(h);
    return true;
  }
  return false;
}
```

##### unparkSuccessor

```java
//释放后继节点
private void unparkSuccessor(Node node) {
  //
  int ws = node.waitStatus;
  if (ws < 0)
    compareAndSetWaitStatus(node, ws, 0);
  
  //获取后继节点
  Node s = node.next;
  //如果后继节点， 如果后续节点是取消状态或者为null,则从tail往前找，直到node,拿得到的就是node的状态正常的后继节点
  if (s == null || s.waitStatus > 0) {
    s = null;
    for (Node t = tail; t != null && t != node; t = t.prev)
      if (t.waitStatus <= 0)
        s = t;
  }
  //如果存在后继节点，唤醒它
  if (s != null)
    LockSupport.unpark(s.thread);
}
```

> 这个后继节点如果为cancel的情况下，为什么不往从前往后找，为啥呢？？？

唤醒后继节点之后，后继节点就会继续执行他的`acquireQueued`自旋操作，竞争锁。

### 共享锁加锁

```java
 public final void acquireShared(int arg) {
   //调用具体实现类的申请锁操作
   if (tryAcquireShared(arg) < 0)
     //申请资源失败，入队列申请锁
     doAcquireShared(arg);
 }

//入队列申请所
private void doAcquireShared(int arg) {
  //和独占锁一样，加入到同步队列中，模式是共享锁
  final Node node = addWaiter(Node.SHARED);
  boolean failed = true;
  try {
    boolean interrupted = false;
    for (;;) {
      //判断前置节点是不是head,如果是，先尝试获取锁
      final Node p = node.predecessor();
      if (p == head) {
        int r = tryAcquireShared(arg);
        if (r >= 0) {
          //获取锁成功之后，将当前节点放到head上，尝试唤醒后续节点
          setHeadAndPropagate(node, r);
          p.next = null; // help GC
          if (interrupted)
            selfInterrupt();
          failed = false;
          return;
        }
      }
      //和排它锁一样，如果获取锁失败，判断是否需要挂起，然后挂起
      if (shouldParkAfterFailedAcquire(p, node) &&
          parkAndCheckInterrupt())
        interrupted = true;
    }
  } finally {
    if (failed)
      cancelAcquire(node);
  }
}

//当前节点获取锁成功的情况下，将他设置成head节点
private void setHeadAndPropagate(Node node, int propagate) {
//将当前Node设置成head
  Node h = head; 
  setHead(node);

  //如果资源还有剩余(propagate>0),尝试唤醒下个节点---共享锁
  if (propagate > 0 || h == null || h.waitStatus < 0 ||
      (h = head) == null || h.waitStatus < 0) {
    Node s = node.next;
    if (s == null || s.isShared())
      doReleaseShared();
  }
}

//唤醒下个节点
private void doReleaseShared() {
  for (;;) {
    Node h = head;
    if (h != null && h != tail) {
      int ws = h.waitStatus;
      //如果当前节点状态是SIGNAL,unpark唤醒下个节点
      if (ws == Node.SIGNAL) {
        if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
          continue;            // loop to recheck cases
        unparkSuccessor(h);
      }
      //如果当前节点状态是0，将他设置成PROPAGATE
      else if (ws == 0 &&
               !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
        continue;                // loop on failed CAS
    }
    if (h == head)                   // loop if head changed
      break;
  }
}
```

>  这个最后的设置状态PROPAGATE，还是有点搞不懂!!!

#### 共享锁释放

```java
public final boolean releaseShared(int arg) {
  //使用子类实现的方法去还资源的数量
  if (tryReleaseShared(arg)) {
    //唤醒像一个节点。
    doReleaseShared();
    return true;
  }
  return false;
}
```



### Condition

> condition实现的功能就像是Object中的wait和notify的功能。

```java
 public class ConditionObject implements Condition, java.io.Serializable {
   //只有当前线程已经获取到锁的情况下才能调用，因为里面有个release操作，如果不是锁的持有者会报错
   public final void await() throws InterruptedException {
     if (Thread.interrupted())
       throw new InterruptedException();
     //将当前线程加入condition队列中
     Node node = addConditionWaiter();
     //释放当前线程的锁，因为可能是可重入锁，所以需要记录下被释放的state值
     int savedState = fullyRelease(node);
     int interruptMode = 0;
     //一直循环或者挂起，知道本线程的node被从condition队列放入同步队列中
     //这个从condition队列放入同步队列的操作是在别的线程执行sign的时候会操作，
     while (!isOnSyncQueue(node)) {
       //如果不在同步队列中，挂起线程
       LockSupport.park(this);
       if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
         break;
     }
     //到当前位置说明已经进入了同步队列，尝试获同步的锁
     if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
       interruptMode = REINTERRUPT;
     if (node.nextWaiter != null) // clean up if cancelled
       unlinkCancelledWaiters();
     if (interruptMode != 0)
       reportInterruptAfterWait(interruptMode);
   }
   
   //通知--只作用于一个节点
    public final void signal() {
      //判断当前线程是否持有锁
      if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
      //获取condition队列中的第一个等待节点，通知该节点
      Node first = firstWaiter;
      if (first != null)
        doSignal(first);
    }
   
   //执行sign
   private void doSignal(Node first) {
     do {
       //如果当前节点后面没有数据，把lastWaiter置空
       if ( (firstWaiter = first.nextWaiter) == null)
         lastWaiter = null;
       first.nextWaiter = null;
       //将first节点从condition队列放入同步队列
     } while (!transferForSignal(first) &&
              (first = firstWaiter) != null);
   }
   
   //将当前节点放入同步队列
   final boolean transferForSignal(Node node) {
        Node p = enq(node);
        int ws = p.waitStatus;
     		//如果这个节点的前置节点为cancel或者设置前置节点状态sign失败，unpark这个节点的线程
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }
   
 }
```





## AQS的其他实现



## 还没有参透的问题

* AQS中的doReleaseShared方法，如果是0状态的情况下，将Node设置成PROPAGATE状态
* unparkSuccessor的时候，如果后继节点是CANCEL状态的时候，为啥是从tail往前找有效节点，而不是从前往后找。

## 参考资料

* https://mp.weixin.qq.com/s/-swOI_4_cxP5BBSD9wd0lA
* 