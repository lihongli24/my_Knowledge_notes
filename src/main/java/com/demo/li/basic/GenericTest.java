package com.demo.li.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lihongli
 * create：2020/3/29 11:15 下午
 */
public class GenericTest {


    public static void main(String[] args){
        List<? super Apple> list = new ArrayList<>();
        list.add(new Apple());
        list.add(new RedApple());

//        list.get(0);
//        list.add(new Fruit());

    }

    public static class Fruit{
        private String name;
    }

    public static class Apple extends Fruit{

    }

    public static class RedApple extends Apple{

    }
}
