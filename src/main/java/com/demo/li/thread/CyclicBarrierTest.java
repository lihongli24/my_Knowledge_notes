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
            Thread thread = new MyThread();
            thread.setName(i + "");
            threadPoolExecutor.submit(thread);
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
