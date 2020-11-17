package com.demo.li.jvm;

/**
 * @author lihongli
 * create：2020/4/29 1:42 下午
 */
public class TryFinallyTest {

    public static int func() {
        try {
            return 0;
        } catch (Exception e){
            return 1;
        } finally {
            return 2;
        }
    }

    public static void main(String[] args){
        Object aa = null;

        String str = (String) aa;
        System.out.println(func());
    }

}
