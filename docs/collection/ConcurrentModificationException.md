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




