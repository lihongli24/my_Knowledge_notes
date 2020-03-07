package com.demo.li.basic;

import java.util.Random;

/**
 * Created by lihongli24 on 2020/3/2.
 */
public class RandomTest {
    public static void main(String[] args) {
        Random random = new Random(1);
        Random random1 = new Random(1);

        for(int i = 0; i< 100; i++){
            System.out.println("0: " + random.nextInt(100) + ", 1: " + random1.nextInt(100));
        }
    }
}
