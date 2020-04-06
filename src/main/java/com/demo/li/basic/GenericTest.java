package com.demo.li.basic;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lihongli
 * create：2020/3/29 11:15 下午
 */
public class GenericTest {


    public static void main(String[] args){
        System.out.println(JSONArray.toJSONString(new Integer[] {}));
    }

    public static class Fruit{
        private String name;
    }

    public static class Apple extends Fruit{

    }

    public static class RedApple extends Apple{

    }
}
