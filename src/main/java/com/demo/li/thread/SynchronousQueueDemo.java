package com.demo.li.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

public class SynchronousQueueDemo {
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        SynchronousQueue<Integer> queue = new SynchronousQueue<>(true);
        Thread thread1 = new Thread(() -> {
            try {
                queue.put(1);
                System.out.println("t1 end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.start();
        Thread.sleep(2000);
        Thread thread2 = new Thread(() -> {
            try {
                queue.put(2);
                System.out.println("t2 end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread2.start();
        Thread.sleep(10000);
        Thread thread3 = new Thread(() -> {
            try {
                System.out.println(queue.take());
//                System.out.println(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread3.start();
    }
}
