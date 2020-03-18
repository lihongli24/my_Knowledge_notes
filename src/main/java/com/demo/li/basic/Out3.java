package com.demo.li.basic;

import java.rmi.Remote;

/**
 * 匿名内部类
 * Created by lihongli24 on 2020/2/23.
 */
public class Out3 {

    private String name = "apple";
    private Integer age = 2;

    private void method1() {
        RemoteInterface remoteInterface = new RemoteInterface() {
            @Override
            public void print() {
                System.out.println("name:" + name + ", age:" + age);
            }
        };
        remoteInterface.print();
    }

    private void method2(){
        name = "asd";
        String namess = new String("aaajjjj");
        RemoteInterface remoteInterface = () ->{System.out.println("name:" + namess + ", age:" + age);};
        remoteInterface.print();
    }

    public static void main(String[] args) {
        Out3 out3 = new Out3();
        out3.method1();
        out3.method2();
    }


}
