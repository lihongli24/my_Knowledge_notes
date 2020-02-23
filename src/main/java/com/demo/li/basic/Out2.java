package com.demo.li.basic;

import lombok.Data;

/**
 * 局部内部类
 * 像局部变量一样的类
 */
@Data
public class Out2 {

    private String paramOut;

    private void outPrint() {
        System.out.println("out out!!!");
    }

    private void method1() {
        String params2 = "123";
        class Inner2 {
            private void print() {
                System.out.println(paramOut);
                System.out.println(params2);
                outPrint();
            }
        }

        Inner2 inner2 = new Inner2();
        inner2.print();
    }

    public static void main(String[] args) {
        Out2 out2 = new Out2();
        out2.setParamOut("11");
        out2.method1();
    }
}
