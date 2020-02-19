# CountDownLatch和CyclicBarrier

## 使用示例对比

### CountDownLatch用例：

```java
package com.demo.li.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CountDownLatchTest {
    private static CountDownLatch countDownLatch = new CountDownLatch(10);

    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 100, 1, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(20));
        //设置核心线程能够超时关闭,要不然这个测试类会一直无法停止
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        for (int i = 0; i < 10; i++) {
            Thread com.demo.li.thread = new MyThread();
            com.demo.li.thread.setName(i + "");
            threadPoolExecutor.submit(com.demo.li.thread);
        }
        countDownLatch.await();
        System.out.println("主线程执行完毕");

    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("开始执行线程" + this.getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("线程" + this.getName() + "count down ");
            countDownLatch.countDown();
        }
    }
}

```

###  CyclicBarrier 使用实例

```java
package com.demo.li.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CyclicBarrierTest {
    private static CyclicBarrier cyclicBarrier = new CyclicBarrier(10);

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 100, 1, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(20));
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        for (int i = 0; i < 10; i++) {
            Thread com.demo.li.thread = new MyThread();
            com.demo.li.thread.setName(i + "");
            threadPoolExecutor.submit(com.demo.li.thread);
        }
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("开始执行线程" + this.getName());
            try {
                Thread.sleep(1000);
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("线程" + this.getName() + "执行完毕");
        }
    }
}

```

> CountDownLatch使用场景，一个线程等其他线程执行countdown,然后再往下走
>
> CyclicBarrier使用场景，多个线程一起等待一个时间点，上面实例是10个线程都到达await()方法。

## 实现原理

### CountDownLatch

### CyclicBarrier