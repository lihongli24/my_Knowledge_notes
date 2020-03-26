package com.demo.li.serialization;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * @author lihongli
 * create：2020/3/25 8:53 下午
 */
public class FastJsonStackOverFlow {


    public static void main(String[] args){
        MyObject myObject = new MyObject();
        JSON.toJSONString(myObject);

    }

    @Data
    static class MyObject{

        private String name;

        public String getJson(){
            return JSON.toJSONString(this);
        }


    }
}
