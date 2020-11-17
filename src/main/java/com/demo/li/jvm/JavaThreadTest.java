package com.demo.li.jvm;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lihongli
 * @date 2020/11/12 12:50
 */
public class JavaThreadTest {

    public static void main(String[] args){
        /**
         * int corePoolSize,
         *                               int maximumPoolSize,
         *                               long keepAliveTime,
         *                               TimeUnit unit,
         *                               BlockingQueue<Runnable> workQueue
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
        for (int i = 0; i< 10; i++){
            threadPoolExecutor.submit(new TestThread());
        }
    }

    public static class TestThread extends Thread{
        public static final Object lock = new Object();
        @Override
        public void run() {
            synchronized (lock){
                while (true){

                }
            }
        }
    }
}
