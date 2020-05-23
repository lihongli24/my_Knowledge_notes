package com.demo.li.exception;

/**
 * @author lihongli
 * create：2020/5/9 4:45 下午
 */
public class ExceptionTest {
    public void test001(){
        try {
            throw new RuntimeException("aa");
        }catch (SecurityException | NullPointerException e){
            System.out.println("cc");
        }
    }
}
