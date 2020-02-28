package com.demo.li.basic;

/**
 * 静态内部类
 * Created by lihongli24 on 2020/2/23.
 */
public class Out4 {

    private String name = "apple";

    private static Integer age = 12;

    public static class Inner4 {
        private void print() {
            System.out.println(age);
        }
    }

    public static void main(String[] args){
        Inner4 inner4 = new Inner4();
        inner4.print();
    }
}
