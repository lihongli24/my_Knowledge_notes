package com.demo.li.aqs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by lihongli on 2019/11/21.
 */
public class SemaphoreTest {

    private static final Semaphore semaphore = new Semaphore(3);

    public static void main(String[] args) {
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(20);
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(10, 100,
            1L, TimeUnit.MILLISECONDS, blockingQueue);
        executorService.allowCoreThreadTimeOut(true);

        //创建10个线程，丢入线程池执行
        for (int i = 0; i < 10; i++) {
            AcquireThread acquireThread = new AcquireThread();
            acquireThread.setName("" + i);
            executorService.submit(acquireThread);
        }
    }


    static class AcquireThread extends Thread {

        @Override
        public void run() {
            System.out.println("线程" + this.getName() + "开始执行。。。");
            try {
                semaphore.acquire();
                System.out.println("线程" + this.getName() + "获取到资源开始运行！！！！！");
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException("线程" + this.getName() + "运行失败");
            } finally {
                semaphore.release();
                System.out.println("线程" + this.getName() + "已释放资源ssssss");
            }

        }
    }


}
