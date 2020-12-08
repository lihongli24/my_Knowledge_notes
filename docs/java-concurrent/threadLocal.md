# ThreadLocal

## 使用

```java
public class ThreadLocalTest {

    public static void main(String[] args){
        ThreadLocal<String> t1 = new ThreadLocal<>();

        new Thread(() -> {
           t1.set("aaa");
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("value is " + t1.get());
        }).start();
    }
}
```

输出：

```java
value is null
```

> threadLocal可以保证一个线程中设置的值，不会影响到别的线程中去，就是保持本线程变量的意思。

## 原理

为什么threadLocal变量保证只访问到当前线程设置的值呢？

看threadLocal中的源码可以看到

```java
ThreadLocal:
// 设置值
public void set(T value) {
  Thread t = Thread.currentThread();
  ThreadLocalMap map = getMap(t);
  if (map != null)
    map.set(this, value);
  else
    createMap(t, value);
}

// 获取值
public T get() {
  Thread t = Thread.currentThread();
  ThreadLocalMap map = getMap(t);
  if (map != null) {
    ThreadLocalMap.Entry e = map.getEntry(this);
    if (e != null) {
      @SuppressWarnings("unchecked")
      T result = (T)e.value;
      return result;
    }
  }
  return setInitialValue();
}


ThreadLocalMap getMap(Thread t) {
  return t.threadLocals;
}
```



```java
Thread：
  
// 在Thread中维护了一个ThreadLocalMap
ThreadLocal.ThreadLocalMap threadLocals = null;

static class ThreadLocalMap {

//  在ThreadLocalMap结构体中维护了一个Entry结构体，他里面也是kv的形式，只是key是一个weakReference修饰的对象。
  static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
      super(k);
      value = v;
    }
  }
  
  private Entry[] table;
}
```



通过上面的代码可以看出来，无论是通过threadLocal对象设置值或者获取值的时候，都会先通过当前所处的线程拿到***threadLocalMap***对象，而这个ThreadLocalMap对象是维护在每个thread中的，所以从threadLocal中获取数据肯定不会拿到别的线程设置进去的数据，做到了线程隔离。

### 弱引用

上面源码里面有看到ThreadLocalMap里面的key是一个***WeakReference***修饰的对象，那么为什么要用弱引用来修饰呢？

![image-20201208234546378](https://tva1.sinaimg.cn/large/0081Kckwgy1glgvpnsntcj318y0okhdt.jpg)



用例中的t1是一个局部变量，它指向了一个ThreadLocal的对象，除此之外，在每个Thread中的ThreadLocalMap里面也有一个key指向了这个TreadLocal对象。

ThreadLocalMap是属于线程的，如果线程是一个常驻线程，那么即使当t1对ThreadLocal的引用消失之后，由于ThreadLocalMap中key对ThreadLocal的引用还是存在的，***如果使用了强引用***那么这个ThreadLocal对象永远都不能被回收（出现内存泄露的问题)

使用弱引用之后，当t1的引用消失之后，因为key对threadLocal的是弱引用，会在下一次垃圾回收之后自动回收，所以这样可以保障threadLocal对象的回收

## 有坑

就算使用了弱引用，保证ThreadLocal对象被回收，但是悲剧的是，ThreadLocalMap里面的key变成null之后，value就再也没有办法通过别人找到这个value了。最后导致因为value没办法回收而出现内存泄露(因为他还在map中，所以不会被回收，但是又不可能被使用)

网上的建议是，在使用完ThreadLocal之后，显示调用它的remove方法。



```java
public void remove() {
  ThreadLocalMap m = getMap(Thread.currentThread());
  if (m != null)
    m.remove(this);
}
```







