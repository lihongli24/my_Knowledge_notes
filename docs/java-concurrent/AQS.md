# AbstractQueuedSynchronizer(AQS)

[toc]

## 文章目的

### 背景

java.util.concurrent包里面提供了很多种锁一样的类，ReentrantLock（可再入锁）、CountDownLatch(共享式的计数阻塞器)、Semaphore(信号量)等等，他们实现的功能各异，但是功能的实现过程中，就有很大一部分逻辑可以抽出来通用，这就是AQS。

### 学习关键字

* 独占锁
* 共享锁
* 自旋
* cas
* condition
* 同步队列
* 加锁和释放锁

> 写这篇文章的目的：因为AQS的实现有点复杂，看了别人的文章之后，当时能理解，但是过段时间就会忘掉，所以结合源码和网上的文章，总结一篇自己的笔记出来。方便日后理解和回忆。

## 使用示例和现成的实现

### 使用示例

### ReentrantLock

## AQS源码分析

## AQS的其他实现

## 参考资料

* https://mp.weixin.qq.com/s/-swOI_4_cxP5BBSD9wd0lA
* 