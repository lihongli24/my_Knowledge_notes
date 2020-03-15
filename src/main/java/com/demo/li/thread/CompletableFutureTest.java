package com.demo.li.thread;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author lihongli
 * create：2020/3/15 10:45 下午
 */
public class CompletableFutureTest {

    ExecutorService executor = Executors.newFixedThreadPool(3);

    @Test
    public void testMethod() {
        String[] orders = {"1", "2", "3", "4", "5", "6"};
        Arrays.stream(orders).forEach(id -> CompletableFuture.supplyAsync(() -> submit(id), executor).exceptionally(e -> {
            System.out.println(e);
            return false;
        }));

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Boolean submit(String order) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("抛一个异常" + order);
    }
}
