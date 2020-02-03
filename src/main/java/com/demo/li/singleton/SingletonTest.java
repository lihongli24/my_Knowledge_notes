package com.demo.li.singleton;

import java.util.concurrent.*;

public class SingletonTest {

    private static InnerClass innerClass = new InnerClass();

    public static InnerClass getInnerClass() {
        return innerClass;
    }

    public static class InnerClass{

    }

    public static void main(String[] args){
        Executor executor = new ThreadPoolExecutor(10, 100, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(20));

        for (int i = 0; i< 10; i++){
            executor.execute(new MyThread());
        }
    }


    static class MyThread extends Thread{
        @Override
        public void run() {
            InnerClass innerClass = SingletonTest.getInnerClass();
            System.out.println(innerClass.hashCode());
        }
    }
}
