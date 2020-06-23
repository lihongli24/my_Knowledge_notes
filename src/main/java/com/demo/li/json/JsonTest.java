package com.demo.li.json;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author lihongli
 * create：2020/6/19 10:30 上午
 */
public class JsonTest {


    public static void main(String[] args) {
        EntityToTest t1 = new EntityToTest();
        t1.setIsAA(true);
        t1.setName("t1");


        EntityToTest t2 = new EntityToTest();
        t2.setIsAA(true);
        t2.setName("t2");


        String t1Str = JSONObject.toJSONString(t1);
        System.out.println(t1Str);

        String t2Str = JSONObject.toJSONString(t2);
        System.out.println(t2Str);

    }

    static class EntityToTest {
        private Boolean isAA;

        private String name;

        private String isBB;


        public Boolean getAA() {
            return isAA;
        }

        public void setAA(Boolean AA) {
            isAA = AA;
        }

        public String getIsBB() {
            return isBB;
        }

        public void setIsBB(String isBB) {
            this.isBB = isBB;
        }

        public Boolean getIsAA() {
            return isAA;
        }

        public void setIsAA(Boolean AA) {
            isAA = AA;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    @Data
    static class EntityToTest002 {
        private Boolean isAA;

        private String name;
    }


}
