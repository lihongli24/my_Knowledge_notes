package com.demo.li.basic;

/**
 * @author lihongli
 * create：2020/2/16 11:45 下午
 */
public class Out {

    private int age;

    public int count;

    class Inner {
        public void print() {
            System.out.println("age" + age + count);
        }
    }
}
