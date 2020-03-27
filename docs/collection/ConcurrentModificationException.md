# Java 集合 Fail-Fast 机制 VS Fail-Safe 机制
## ConcurrentModificationException
出现场景：
```java
public class ArrayListTest{

    public static void main(String[] args){
        List<String> list = Lists.newArrayList("aa", "bb", "cc");
        for(String string : list){
            System.out.println(string);
            list.remove(string);
        }
    }
}
```
运行完得到的异常为：
```java
Exception in thread "main" java.util.ConcurrentModificationException
	at java.util.ArrayList$Itr.checkForComodification(ArrayList.java:909)
	at java.util.ArrayList$Itr.next(ArrayList.java:859)
	at com.demo.li.basic.ArrayListTest.main(ArrayListTest.java:15)
```
为什么出现这种情况：
这是List的 fail-fast机制,是java集合的一种快速失败的机制，针对多线程环境下，两个线程同时操作,一个在遍历数据，一个在删除数据的情况下，导致遍历的那个线程无法正确的遍历的集合，所以采取的快速失败的处理方式。

> 解决的办法可以，使用Iterator提供的remove方法，因为在那里面他会去修改他的`expectedModCount`

```java
 public void remove() {
    if (lastRet < 0)
        throw new IllegalStateException();
    checkForComodification();

    try {
        ArrayList.this.remove(lastRet);
        cursor = lastRet;
        lastRet = -1;
        expectedModCount = modCount;
    } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
    }
}
```

> 个人总结：Fail-Fast是一种遇到异常情况下，快速抛出异常，防止程序继续执行下去。
> 为了防止在遍历过程中别人修改列表导致遍历结果有问题,而采取的一种措施。看着有点简单暴力


那么有没有别的办法，在当前线程遍历某个List的情况下，不因为别的线程操作了当前List而导致遍历结果有问题呢

## fail-safe机制
上面的问题可以使用`CopyOnWriteArrayList`来实现
> 相应的，为了实现线程安全性，CopyOnWriteArrayList比较费空间。


```java
public class CopyOnWriteArrayList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    
    //具体数据存放的Obect数组
    private transient volatile Object[] array;

    //获取迭代器的时候，把存放数据的数组传进来
     public Iterator<E> iterator() {
        return new COWIterator<E>(getArray(), 0);
    }


    //add 方法，
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        //使用锁来保证线程安全性
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            //创建一个新的数组，并且将原先的数据拷贝进去
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            //把需要存放的数据放进去
            newElements[len] = e;
            //将CopyOnWriteArrayList的arrayList指向新创建出来的数组
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sets the array.
     */
    final void setArray(Object[] a) {
        array = a;
    }

    //CopyOnWriteArrayList的迭代器，
    static final class COWIterator<E> implements ListIterator<E> {
        /** Snapshot of the array */
        private final Object[] snapshot;
        /** Index of element to be returned by subsequent call to next.  */
        private int cursor;

        //在迭代器内，使用snapshot指向被遍历的访问数据的数组
        private COWIterator(Object[] elements, int initialCursor) {
            cursor = initialCursor;
            snapshot = elements;
        }
    }
}

```
##### 合理的总结
1. COWIterator里面维护的snapshot执行创建迭代器的时候，CopyOnWriteArrayList保存数据的数组
2. 新增的时候，创建出来一个新的数组
3. CopyOnWriteArrayList中的array指向了新的数组
4. COWIterator迭代器，依旧遍历这老的那个数组，遍历过程不会因为新增加了或者删除了数据导致的遍历异常 





