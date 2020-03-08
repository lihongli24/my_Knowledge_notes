package com.demo.li.basic;

import org.testng.collections.Lists;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author lihongli
 * create：2020/3/7 5:26 下午
 */
public class ArrayListTest{

    public static void main(String[] args){
        List<String> list = Lists.newArrayList("aa", "bb", "cc");
//        for(String string : list){
//            System.out.println(string);
//            list.remove(string);
//        }
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>(list);
        for (String string : copyOnWriteArrayList){
            System.out.println(string);
            copyOnWriteArrayList.remove(string);
        }

    }
}
