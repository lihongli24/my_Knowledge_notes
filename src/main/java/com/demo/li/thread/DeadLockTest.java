package com.demo.li.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lihongli
 * create：2020/3/5 1:03 下午
 */
public class DeadLockTest {

    private static void method01() {
        synchronized (Integer.class) {
            System.out.println("Integer");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (String.class) {
                System.out.println("String");
            }
        }


    }

    private static void method02() {
        synchronized (String.class) {
            System.out.println("String");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (Integer.class) {
                System.out.println("Integer");
            }
        }


    }


    public static class Thread01 extends Thread {
        @Override
        public void run() {
            DeadLockTest.method01();
        }
    }

    public static class Thread02 extends Thread {
        @Override
        public void run() {
            DeadLockTest.method02();
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(100));
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        threadPoolExecutor.submit(new Thread01());
        threadPoolExecutor.submit(new Thread02());


    }


}
