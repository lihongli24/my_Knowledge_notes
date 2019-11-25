## 目的
我们平常使用线程的方式
1. 继承Thread
2. 实现Runnable
但是这两种方式都有一个问题，那就是`不能拿到运行的返回值`，除非使用共享变量等复杂的方式来间接的实现。

所以，jdk5开始就提供了`Callable`和`Future`

## 什么是callable(可调用并且有返回值的一种类)
```
@FunctionalInterface
public interface Callable<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    V call() throws Exception;
}
```

>为了解决Runnable不能做返回值的限制，所以有了Callable。<br/>但是我觉得，Callable并不是和Runnable或者Thread一个层面的，它仅仅表示一个可以调用，并且会有返回值的一种类。<br/>想实现有返回值的线程还必须依靠`FutureTask`

## 什么是 FutureTask、Future 
```java
public class FutureTask<V> implements RunnableFuture<V>

public interface RunnableFuture<V> extends Runnable, Future<V>


public interface Future<V> {
    boolean cancel(boolean mayInterruptIfRunning);
    boolean isCancelled();
    boolean isDone();
    V get() throws InterruptedException, ExecutionException;
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}

```
从上面的代码可以看出来，<br/>
FutureTask实现了RunnableFuture ---》<br/> RunnableFuture又是继承了Runnable和Future ---》<br/> 所以FutureTask就是Runnable和Future接口的实现类。<br/>
所以，她既是`我们原先使用的线程`又可以对`这个线程做取消，获取返回值等操作`。

## 具体是怎么实现的
#### 常用的使用方式：
```java
public class FutureTest {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Task task = new Task();
        Future<Integer> result = executor.submit(task);
        executor.shutdown();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        System.out.println("主线程在执行任务");

        try {
            System.out.println("task运行结果" + result.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("所有任务执行完毕");
    }
}

class Task implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("子线程在进行计算");
        Thread.sleep(3000);
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            sum += i;
        }
        return sum;
    }
}
```
上面这段代码是我们使用Future和Callable的一般使用方式，其中用到了
```java
//根据阿里的开发手册不推荐直接使用Executors.newCachedThreadPool()
ExecutorService executor = Executors.newCachedThreadPool();
executor.submit(Callable callable)
```

具体调用的代码内容如下：
#### 1. 将线程丢进线程池，获取到线程的引用。
```java
创建一个线程池执行器
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
    
public class ThreadPoolExecutor extends AbstractExecutorService
....


//调用submit将线程丢进线程池
public abstract class AbstractExecutorService implements ExecutorService {
...
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new FutureTask<T>(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask<T>(callable);
    }

    public Future<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

 
    public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

   
    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }
    
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask<T>(callable);
    }
...
    
}



public class FutureTask<V> implements RunnableFuture<V> {
。。。
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable
    }
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;       // ensure visibility of callable
    }
。。。
}


public class Executors {
...
    public static <T> Callable<T> callable(Runnable task, T result) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<T>(task, result);
    }
...

    static final class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;
        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }
        public T call() {
            task.run();
            return result;
        }
    }
    
}
```
我们可以在AbstractExecutorService中提交Callable或者Runnable;
最终，`会通过Executors.callable用RunnableAdapter将Runnable也转化成Callable`。

在上面的代码里面可以看到，在submit的时候，
1. 无论是Runnable还是Callable最后都被封装成了RunnableFuture(`一个可以获取到返回值的线程`)
2. 调用`execute` -- > 将这个任务丢进线程池，用线程池来协调线程的运行。
3. 将RunnableFuture做为返回值返回出去---->我们可以使用这个引用获取到线程的执行结果或者取消这个线程。
```java
public <T> Future<T> submit(Callable<T> task) {
    if (task == null) throw new NullPointerException();
    RunnableFuture<T> ftask = newTaskFor(task);
    execute(ftask);
    return ftask;
}
```

#### 执行线程
要获取线程的执行结果，就得讲到`FutureTask`,仔细看上面的代码，其实最终的无论是Callable还是Runnable最终转化的都是FutureTask
```java
protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    return new FutureTask<T>(callable);
}


public class FutureTask<V> implements RunnableFuture<V> {

/**
     * The run state of this task, initially NEW.  The run state
     * transitions to a terminal state only in methods set,
     * setException, and cancel.  During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)). Transitions from these intermediate to final
     * states use cheaper ordered/lazy writes because values are unique
     * and cannot be further modified.
     *
     * Possible state transitions:
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     */
    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;

    /** The underlying callable; nulled out after running */
    private Callable<V> callable;
    /** The result to return or exception to throw from get() */
    private Object outcome; // non-volatile, protected by state reads/writes
    /** The thread running the callable; CASed during run() */
    private volatile Thread runner;
    /** Treiber stack of waiting threads */
    private volatile WaitNode waiters;

    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable
    }
}
```

| 属性     | 意义                                                         |
| -------- | ------------------------------------------------------------ |
| callable | 本线程需要执行的任务，会在最后有一个返回值                   |
| outcome  | callable执行的返回值会被放在outcome里面                      |
| runner   | 在调用FutureTask的run方法是，会将当前线程的放在这个runner中，会在取消线程时有作用 |
| waiters  | 等待中的线程，在本线程执行完成之后，会唤醒下一个等待线程     |

```java
public void run() {
    if (state != NEW ||
        !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                     null, Thread.currentThread()))
        return;
    try {
        Callable<V> c = callable;
        if (c != null && state == NEW) {
            V result;
            boolean ran;
            try {
                result = c.call();
                ran = true;
            } catch (Throwable ex) {
                result = null;
                ran = false;
                setException(ex);
            }
            if (ran)
                set(result);
        }
    } finally {
        // runner must be non-null until state is settled to
        // prevent concurrent calls to run()
        runner = null;
        // state must be re-read after nulling runner to prevent
        // leaked interrupts
        int s = state;
        if (s >= INTERRUPTING)
            handlePossibleCancellationInterrupt(s);
    }
}


protected void set(V v) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        outcome = v;
        UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
        finishCompletion();
    }
}


protected void setException(Throwable t) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        outcome = t;
        UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
        finishCompletion();
    }
}
    
```
1. 如果当前state还是NEW的话，现将runnerOffset指向当前线程
2. 调用callable的call方法
3. 如果调用成功，调用set方法，状态设置为COMPLETING -》将返回值设置进去，-》状态设置为NORMAL
4. 如果调用失败，将状态设置成COMPLETING-》将异常放入返回值中-》将状态设置成EXCEPTIONAL

#### 获取结果
```java
FutureTask 中：
/**
 * @throws CancellationException {@inheritDoc}
 */
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING)
        s = awaitDone(false, 0L);
    return report(s);
}

/**
 * @throws CancellationException {@inheritDoc}
 */
public V get(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
    if (unit == null)
        throw new NullPointerException();
    int s = state;
    if (s <= COMPLETING &&
        (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
        throw new TimeoutException();
    return report(s);
}

 /**
 * Returns result or throws exception for completed task.
 *
 * @param s completed state value
 */
@SuppressWarnings("unchecked")
private V report(int s) throws ExecutionException {
    Object x = outcome;
    if (s == NORMAL)
        return (V)x;
    if (s >= CANCELLED)
        throw new CancellationException();
    throw new ExecutionException((Throwable)x);
}
```
1. 如果当前状态比COMPLETING小，也就是NEW，任务还在执行中。等待任务执行完成
2. 如果执行成功，状态为NORMAL，将outcome作为返回值返回
3. 如果状态为取消，抛出CancellationException
4. 以上都不是，将outcome作为异常抛出