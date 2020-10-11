# threadpool源码

[toc]
主要是照着这个[帮助文档](http://www.throwable.club/2019/07/15/java-concurrency-thread-pool-executor/)看下来的，这里写出自己阅读代码的过程，方便之后看线程池的时候印象更深一些

## ThreadPoolExecutor
![](https://tva1.sinaimg.cn/large/007S8ZIlgy1gjjiaxtps3j30qo0ks3zd.jpg)

主要的功能有提交任务，或者对其进行取消等操作.

### 提交任务入口
按照最上面的`Executor`的定义来说，需要实现
```java
 void execute(Runnable command);
```
 这个提交任务的接口，只能异步启动一个线程，执行任务，但是主线程无法获取到新线程的返回值，也无法对其进行中断等操作。
 之后在ExecutorService中进行了扩充，提供了submit方法。

 ```java
 <T> Future<T> submit(Callable<T> task);
 <T> Future<T> submit(Runnable task, T result);
 Future<?> submit(Runnable task);
 ```
 这三个都会返回对应的Future，使得主线程有入口可以操作新启动的线程。

 这三个方法的大致实现差不多是这样的，具体关于FutureTask之类的只是可以看这个目录里面的Callble和Feature.md里面的内容,
 `AbstractExecutorService`中实现如下：
 ```java
 public <T> Future<T> submit(Callable<T> task) {
    if (task == null) throw new NullPointerException();
    RunnableFuture<T> ftask = newTaskFor(task);
    execute(ftask);
    return ftask;
}
 ```
 大致的内容是，将任务task封装成一个RunnableFuture,然后调用execute方法进行操作。

### execute 将任务丢如线程池执行


对于这个execute方法，就是在`Executor`中定义的

```java
public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();

        int c = ctl.get();
        // 如果，当前的工作线程数量小于核心线程数，已核心线程的方式添加一个worker线程
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
    // 如果核心线程数用完了，往workQueue中添加任务，
    // 这个workQueue中的任务，会在后续的操作中取出来去执行,具体见worker节点内容
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            // 如果当前工作线程数量为0，添加一个空任务，保证存在工作线程
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        // 如果核心线程数用完了，并且workqueue空间也使用完了之后，使用非核心线程数的方式启动
        else if (!addWorker(command, false))
        // 如果非核心线程数也不够用了，那就使用拒绝策略
            reject(command);
    }
```

* addWorker(command, true) 添加核心线程任务
* workQueue.offer(command) 将任务添加到工作队列中，等待后续获取后来执行
* addWorker(null, false); 以非核心线程的方式添加任务

到这里，差不多添加任务到线程池的步骤已经完成了，那么，添加进去的任务是怎么执行的呢？所谓的线程池是怎么关联线程的呢？

### addWorker 增加工作线程

上面，核心线程和非核心线程都是通过addWorker的方法来实现的，代码如下:

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // Check if queue empty only if necessary.
        // 如果当前线程池处于shutdown状态下，不接受新的任务
        if (rs >= SHUTDOWN &&
            ! (rs == SHUTDOWN &&
                firstTask == null &&
                ! workQueue.isEmpty()))
            return false;

        for (;;) {
            // 判断工作线程数量，
           // 如果是创建核心线程的情况，当前工作线程数不能超过coreSize
           // 如果创建的是非核心线程，当前工作线程数量不能超过maximumPoolSize
           // 否则返回false,表示不能添加任务
            int wc = workerCountOf(c);
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            // 使用case修改工作线程数量，如果成功，可以跳出retry去添加新的工作线程
            if (compareAndIncrementWorkerCount(c))
                break retry;
            c = ctl.get();  // Re-read ctl
            // 如果当前的线程池的状态有变化，调到最外层的retry重新执行
            if (runStateOf(c) != rs)
                continue retry;
            // else CAS failed due to workerCount change; retry inner loop
        }
    }

    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
        // 创建工作线程，
        w = new Worker(firstTask);
        final Thread t = w.thread;
        if (t != null) {
            // 使用当前线程池实例中的全局锁加锁
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // Recheck while holding lock.
                // Back out on ThreadFactory failure or if
                // shut down before lock acquired.
                int rs = runStateOf(ctl.get());

                // 在确认线程池运行状态正常等情况下，往workers这个hashSet容器里面添加当前创建的worker节点
                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();
                    workers.add(w);
                    int s = workers.size();
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }

            // 如果节点添加成功之后，执行线程的start方法，启动线程。
            if (workerAdded) {
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        // 如果添加失败，执行添加工作线程失败的方法。
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}
```

在上面的代码中，主要做了两件事情，
1. 创建了一个worker
2. 启动worker.thread

> 那么这个thread启动了之后，他到底做了一些什么操作
> 带着这个疑问可以往下看


worker的构造函数如下：
```java
Worker(Runnable firstTask) {
    setState(-1); // inhibit interrupts until runWorker
    this.firstTask = firstTask;
    this.thread = getThreadFactory().newThread(this);
}
```
这里面的thread使用的是threadFacory的方法来创建的，并且在请求的时候传入了this

下面的代码是Executors里面的DefaultThreadFacory的代码，在他的newThread方法中，构造出了一个thread， 并传入了r(`也就是我们上面传入的this,也就是当前的worker对象)

```java
 /**
 * The default thread factory
 */
static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DefaultThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                                Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                        poolNumber.getAndIncrement() +
                        "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                                namePrefix + threadNumber.getAndIncrement(),
                                0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
```

Thread.java中的代码
```java
public Thread(ThreadGroup group, Runnable target, String name,
                long stackSize) {
    init(group, target, name, stackSize);
}


// 执行thread的时候，如果target不为空，就会执行target中的run方法
 @Override
public void run() {
    if (target != null) {
        target.run();
    }
}
```

所以在介绍完了上面的代码之后，也就能看出来，上面添加完worker之后，调用的thread.start最终执行的是什么代码，也就是这个thread里面的target(woker对象)的run方法。

### ThreadExector.Worker.runWorker() 执行工作线程内容

ThreadExector.Worker中的方法

```java
// run方法调用到的是自己的runWorker方法

// worker继承自AQS,可以使用AQS的模板来进行上锁等操作的实现
private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable

Worker(Runnable firstTask) {
    setState(-1); // inhibit interrupts until runWorker
    this.firstTask = firstTask;
    this.thread = getThreadFactory().newThread(this);
}

public void run() {
    runWorker(this);
}

final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    // 由于Worker初始化时AQS中state设置为-1，这里要先做一次解锁把state更新为0，允许线程中断
    w.unlock(); // allow interrupts
    boolean completedAbruptly = true;
    try {
        // 循环获取任务，这个getTask就是核心线程使用完了之后，优先放入的workqueue
        while (task != null || (task = getTask()) != null) {
            // 对当前的工作线程上锁
            w.lock();
            // If pool is stopping, ensure thread is interrupted;
            // if not, ensure thread is not interrupted.  This
            // requires a recheck in second case to deal with
            // shutdownNow race while clearing interrupt
            if ((runStateAtLeast(ctl.get(), STOP) ||
                    (Thread.interrupted() &&
                    runStateAtLeast(ctl.get(), STOP))) &&
                !wt.isInterrupted())
                wt.interrupt();
            try {
                // 提供的一些回调方法,在任务执行的各个阶段
                beforeExecute(wt, task);
                Throwable thrown = null;
                try {
                    // 执行具体任务的方法,
                    //这里调用的是run方法，因为线程池中管理的线程是worker线程，使用这些worker线程来执行每个业务线程的run方法里面的业务逻辑代码
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x; throw x;
                } catch (Error x) {
                    thrown = x; throw x;
                } catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } finally {
                    afterExecute(task, thrown);
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally {
        // 任务执行完成之后，执行退出逻辑
        processWorkerExit(w, completedAbruptly);
    }
}
```
* 来自参考文档中的图片
![](https://tva1.sinaimg.cn/large/007S8ZIlgy1gjlrq9fnc2j30q80l8q4b.jpg)


里面有两个比较重要的内容
1. getTask()
2. processWorkerExit

### getTask 从任务队列中获取需要执行的任务
getTask的主要功能是从 workQueue中获取到之前添加进去的任务，
> 这里的workQueue需要和我们的worker区分开来

workQueue的定义如下：
```java
BlockingQueue<Runnable> workQueue
```
他是一个用于存放Runnable的一个BlockingQueue, 上面介绍的worker是一个工作线程执行的代码，所以两个work关系不大。
```java
private Runnable getTask() {
    boolean timedOut = false; // Did the last poll() time out?

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // Check if queue empty only if necessary.
        // 如果线程池处于停止的状态下，并且workQueue中的任务为空，
        // 减少工作线程的数量，返回未获取到task
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }

        // 获取到当前工作线程的数量
        int wc = workerCountOf(c);

        // Are workers subject to culling?
        // 允许核心线程超时，或者当前的工作线程数量超过了核心线程数量限制时，表示支持扑杀 culling
        // 允许核心线程超时的意思是，如果核心线程出现空闲的情况，也会被结束
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
        if ((wc > maximumPoolSize || (timed && timedOut))
            && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;
        }

        // 从任务队列中获取任务，会按照是否进行超时判断分成两个方法
        try {
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                workQueue.take();
            // 如果获取到任务之后，就直接返回
            if (r != null)
                return r;
            // 如果没获取到任务，将timeout设置成true，那么在下次循环里面这个timeOut就会起作用
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```
### processWorkerExit 处理worker线程的退出
在工作线程获取不到需要执行的任务的时候，必要的时候需要对当前工作线程进行退出处理




