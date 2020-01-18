# DelayQueue

## 大致：
作用：延迟队列，放在队列中的数据，只有等到delay的时间到了之后才能被take到。

```java
public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
    implements BlockingQueue<E> {

    private final transient ReentrantLock lock = new ReentrantLock();
    private final PriorityQueue<E> q = new PriorityQueue<E>();
    
    .....
}
```

DelayQueue中只能存放继承了Delayed的对象，因为在获取对象的时候需要判断是不是到了可以取出来的时间了。

```java
public interface Delayed extends Comparable<Delayed> {
    /**
     * Returns the remaining delay associated with this object, in the
     * given time unit.
     *
     * @param unit the time unit
     * @return the remaining delay; zero or negative values indicate
     * that the delay has already elapsed
     */
    long getDelay(TimeUnit unit);
}
```
Delayed对象满足下面两个点：
1. 对象需要实现一个getDelay方法
2. 因为他继承自Compareable类，所以必须实现compare方法。

这两个点在后续的存放对象和获取对象的过程取到了很大的作用。


## 如何实现延迟
### 存放对象
```java
DelayQueue:

/**
插入数据
*/
public boolean offer(E e) {
//并发情况下加锁操作
  final ReentrantLock lock = this.lock;
  lock.lock();
  try {
  //内部维护的一个PriorityQueue，后续会讲
    q.offer(e);
    //如果当前放进去的是第一个，需要唤醒正在阻塞等到获取数据的线程，AQS中的Condition概念
    if (q.peek() == e) {
      leader = null;
      available.signal();
    }
    return true;
  } finally {
    lock.unlock();
  }
}
```
上面的代码中q.offer(e)才是比较核心的代码。所以看到类PriorityQueue中(带优先级的queue)
```java
PriorityQueue:

//放数据进去
 public boolean offer(E e) {
   if (e == null)
     throw new NullPointerException();
   modCount++;
   int i = size;
   //如果存放的数据到了queue当前的容量，需要对它做扩容
   if (i >= queue.length)
     grow(i + 1);
   //存放数量大小+1
   size = i + 1;
   //如果当前队列中没有数据，将当前数据放到第一个（不需要去做比较）
   if (i == 0)
     queue[0] = e;
   else
     //如果队列中有数据了，放入当前数据的时候需要找到对应的位置
     siftUp(i, e);
   return true;
 }
```

#### 扩容

```java
//扩容逻辑
private void grow(int minCapacity) {
  int oldCapacity = queue.length;
  // Double size if small; else grow by 50%
  //如果小于64。扩容一倍。不知道这个+2是什么用意
  //如果已经保存的数据超过64了，扩容50%
  int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                   (oldCapacity + 2) :
                                   (oldCapacity >> 1));
  // overflow-conscious code
  //对最大容量做处理，
  if (newCapacity - MAX_ARRAY_SIZE > 0)
    //看下面的逻辑，对最大值做处理
    newCapacity = hugeCapacity(minCapacity);
  //按照新的容量生成新的queue
  queue = Arrays.copyOf(queue, newCapacity);
}

//对最大值做处理，最大能支持到 Integer的最大值。
private static int hugeCapacity(int minCapacity) {
  if (minCapacity < 0) // overflow
    throw new OutOfMemoryError();
  return (minCapacity > MAX_ARRAY_SIZE) ?
    Integer.MAX_VALUE :
  MAX_ARRAY_SIZE;
}
```
#### 寻址放数据

```java
//筛选合适的位置，放入新的元素
private void siftUp(int k, E x) {
//支持两种方式，
//一种是传入comparator，
//另一种是当X实现compareable接口
  if (comparator != null)
    siftUpUsingComparator(k, x);
  else
    siftUpComparable(k, x);
}


//筛选位置。用到了堆的逻辑，小顶堆
// 堆的逻辑这个视频讲的很好 https://www.bilibili.com/video/av47196993?from=search&seid=11328586015539132708
private void siftUpComparable(int k, E x) {
  Comparable<? super E> key = (Comparable<? super E>) x;
  //k是假设要插入的位置
  while (k > 0) {
    //找到要插入位置的父位置
    int parent = (k - 1) >>> 1;
    //取出父位置的节点数据
    Object e = queue[parent];
    //将如果要插入的节点比父节点的数据大，跳出循环，将key插入当前选出的k位置，
    if (key.compareTo((E) e) >= 0)
      break;
    //如果是要插入的节点的值比父节点要小，将父节点置换到当前应该插入的位置，然后将k(当前应该插入的位置)指向原来父节点的位置，一级一级向上找，保证最小的最靠前
    queue[k] = e;
    k = parent;
  }
  //将我们要插入的数据放入上面遍历到的最终位置
  queue[k] = key;
}

//逻辑和上面一样，只是比较器不一样
private void siftUpUsingComparator(int k, E x) {
  while (k > 0) {
    int parent = (k - 1) >>> 1;
    Object e = queue[parent];
    if (comparator.compare(x, (E) e) >= 0)
      break;
    queue[k] = e;
    k = parent;
  }
  queue[k] = x;
}
```

经过上面的操作，我们保证延迟时间是从小到大存放在了这个queue中了.

> Tips:
>
> 堆
>
> 小顶堆
>
> [其实就是对象数组，搞法很很巧妙](https://www.bilibili.com/video/av47196993?from=search&seid=11328586015539132708)



### 获取数据

```java
DelayQueue:

//获取数据
public E take() throws InterruptedException {
  final ReentrantLock lock = this.lock;
  //加锁获取
  lock.lockInterruptibly();
  try {
    for (;;) {
			//获取第一个值，因为前面做了排序，所以第一个肯定是最快到时间的
      E first = q.peek();
      //如果取不到数据，使用Condition来阻塞队列
      if (first == null)
        available.await();
      else {
        //取出这个数据的delay值，先判断下是不是到期了----只是拿出来，不会导致数据出队列
        long delay = first.getDelay(NANOSECONDS);
        if (delay <= 0)
          //如果到期了，poll出
          return q.poll();
        //如果没到期，就当没取到
        first = null; // don't retain ref while waiting
        //如果当前已经有一个线程再尝试获取了，其他的线程阻塞，等待唤醒
        //leader和其他线程的区别就是leader的await方法带超时时间
        if (leader != null)
          available.await();
        else {
          //如果当前还没有leader线程，把当前线程设置成leader
          Thread thisThread = Thread.currentThread();
          leader = thisThread;
          try {
            //进行线程等待---带超时时间
            available.awaitNanos(delay);
          } finally {
            //将leader设置成null,防止当前线程在for循环里面重试的时候，因为leader已经设置过了导致大家都处于await，走不下去了
            if (leader == thisThread)
              leader = null;
          }
        }
      }
    }
  } finally {
    //leader线程处理完了之后，需要唤醒还阻塞的线程尝试获取值
    if (leader == null && q.peek() != null)
      available.signal();
    lock.unlock();
  }
}
```

> Tips:
>
> 1. 根据getDelay出来的值判断是不是已经到期了
> 2. 只有一个线程为成为leader线程，减少多个线程一起获取带来的问题
> 3. available AQS中的Condition,有问题可以回看目录中AQS的文章

#### poll过程

```java
PriorityQueue:

//从PriorityQueue获取值，
//因为获取走一个值，堆不完整了，需要进行堆的恢复
public E poll() {
  if (size == 0)
    return null;
  //完成两步操作
  //1. size自减
  //2. s = size -1,因为s是index
  int s = --size;
  //修改modCount
  modCount++;
  //取出第一个，这个是返回值
  E result = (E) queue[0];
  //拿到最后一个,因为取走了第一个，所以他的位置肯定也会变
  E x = (E) queue[s];
  queue[s] = null;
  if (s != 0)
    //进行堆的转化
    siftDown(0, x);
  return result;
}



//转化堆，又是按照比较方式分成2个方式
private void siftDown(int k, E x) {
  if (comparator != null)
    siftDownUsingComparator(k, x);
  else
    siftDownComparable(k, x);
}

@SuppressWarnings("unchecked")
private void siftDownComparable(int k, E x) {
  //取出最后一个值，最后需要把它往前面挪出来的空位上填
  Comparable<? super E> key = (Comparable<? super E>)x;
  //很巧妙，size/2就能算到第一个叶子节点，判断也只要到他位置
  int half = size >>> 1;        // loop while a non-leaf
  while (k < half) {
    //大致逻辑，比较k位置的两个子节点，选出比较小的，
    //1. 值赋值到变量c,
    //2. 位置赋值到变量child
    int child = (k << 1) + 1; // assume left child is least
    Object c = queue[child];
    int right = child + 1;
    if (right < size &&
        ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0)
      c = queue[child = right];
    //如果选出来的c值比key的大，直接返回，k的位置可以直接放key---不知道什么情况下会出现这种情况
    if (key.compareTo((E) c) <= 0)
      break;
    //将选出来的比较小的c放到目前空出来的位置
    //将判断的位置k赋值成child的值，下一个循环就是要对他的子节点进行判断
    queue[k] = c;
    k = child;
  }
  //将key放入目前空出来的位置
  queue[k] = key;
}

//和上面一样，只是比较方式不一样
@SuppressWarnings("unchecked")
private void siftDownUsingComparator(int k, E x) {
  int half = size >>> 1;
  while (k < half) {
    int child = (k << 1) + 1;
    Object c = queue[child];
    int right = child + 1;
    if (right < size &&
        comparator.compare((E) c, (E) queue[right]) > 0)
      c = queue[child = right];
    if (comparator.compare(x, (E) c) <= 0)
      break;
    queue[k] = c;
    k = child;
  }
  queue[k] = x;
}
```



> Tips:
>
> 又是一套堆的操作，大牛就是大牛
>
> if (key.compareTo((E) c) <= 0)
>       break;
>
> 不知道什么情况下会出现这种情况