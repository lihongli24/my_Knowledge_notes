package com.demo.li.algorithm.leetcode.twopoint;

import lombok.AllArgsConstructor;

/**
 * @author lihongli
 * create：2020/5/28 11:59 下午
 */
public class CycleLink {


    public static void main(String[] args) {
        Link link = null, link2Loop;
        for (int i = 0; i < 10; i++) {
            link = new Link(i, link);
            if(i == 5){
                link2Loop = link;
            }
        }



    }

    @AllArgsConstructor
    static class Link {
        private int value;
        private Link next;
    }
}
