package com.demo.li.thread;

/**
 * @author lihongli
 * @date 2020/12/8 23:31
 */
public class ThreadLocalTest {

    public static void main(String[] args){
        ThreadLocal<String> t1 = new ThreadLocal<>();

        new Thread(() -> {
           t1.set("aaa");
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("value is " + t1.get());
        }).start();
    }
}
