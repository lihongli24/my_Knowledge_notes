package com.demo.li.basic;

/**
 * 成员内部类
 * 像成员变量一样的类
 *
 * @author lihongli
 *         create：2020/2/16 11:45 下午
 */
public class Out {

    private int age = 1;

    public int count = 2;

    private void outPrint() {
        System.out.println("out out!!!");
    }

    private class Inner {
        public void print() {
            outPrint();
            System.out.println("age" + age + count);
        }
    }

    public static void main(String[] args) {
        Inner inner = new Out().new Inner();
        inner.print();

    }
}
