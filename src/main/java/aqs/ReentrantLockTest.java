package aqs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lihongli on 2019/11/19.
 */
public class ReentrantLockTest {

    public static void main(String[] args) {

        //创建线程池
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(20);
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(10, 100,
            1L, TimeUnit.MILLISECONDS,blockingQueue);
        executorService.allowCoreThreadTimeOut(true);

        //创建10个线程，丢入线程池执行
        for (int i = 0; i < 10; i++){
            TryLockThread tryLockThread = new TryLockThread();
            tryLockThread.setName("" + i);
            executorService.submit(tryLockThread);
        }
    }

    //线程，执行时先获取锁，休眠1s
    static class TryLockThread extends Thread {

        private static ReentrantLock reentrantLock = new ReentrantLock();

        @Override
        public void run() {
            try {
                System.out.println("线程" + this.getName() + "开始获取锁=========");
                reentrantLock.lock();
                System.out.println("线程" + this.getName() + "成功获取锁!!!!!!!!!!!!!");
                Thread.sleep(1000L);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
                System.out.println("线程" + this.getName() + "释放锁ssssssssss");
            }
        }
    }

}
