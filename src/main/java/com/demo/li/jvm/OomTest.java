package com.demo.li.jvm;

/**
 * Created by lihongli24 on 2020/3/9.
 */
public class OomTest {

    public static void main(String[] args){
        int i = 0;

        while (true){
            String str = new String("aaooooooooooooooooooooooooooooooooooooooooooooooooooooo" + i);
            str.intern();
        }
    }
}
