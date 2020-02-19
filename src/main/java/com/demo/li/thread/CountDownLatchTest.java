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
            Thread thread = new MyThread();
            thread.setName(i + "");
            threadPoolExecutor.submit(thread);
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
