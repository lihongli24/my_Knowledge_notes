package com.demo.li.algorithm.leetcode.slidingwindow;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 最小包含子串
 *
 * @author lihongli
 * create：2020/5/28 1:19 下午
 */
public class MinConstantString {
    /**
     * 获取orig中包含dest的最小子串
     *
     * @param orig 原始字符串
     * @param dest 目标字符串
     * @return
     */
    private static String getMinConstantsString(String orig, String dest) {
        char[] orgArray = orig.toCharArray();
        char[] destArray = dest.toCharArray();

        Map<Character, Integer> window = new HashMap<>();

        //填充需要的
        Map<Character, Integer> need = new HashMap<>();
        for (char descChar : destArray) {
            need.put(descChar, need.getOrDefault(descChar, 0) + 1);
        }

        int length = Integer.MAX_VALUE;
        int start = 0;
        int left = 0, right = 0;
        //窗口内的有效记录数
        int valid = 0;
        while (right < orgArray.length) {
            //准备加入window的字符
            char chartToAdd = orgArray[right];
            right++;

            if (need.containsKey(chartToAdd)) {
                window.put(chartToAdd, window.getOrDefault(chartToAdd, 0) + 1);
                if (window.get(chartToAdd).equals(need.get(chartToAdd))) {
                    valid++;
                }
            }
//            System.out.println(String.format("window: [%d, %d)", left, right));

            //当可以收缩的时候
            while (valid == need.size()) {
                if (right - left < length) {
                    length = right - left;
                    start = left;
                }

                //准备从widow中删除的字符串
                char chartToRemove = orgArray[left];
                left++;
                if (need.containsKey(chartToRemove)) {
                    if (window.get(chartToRemove).equals(need.get(chartToRemove))) {
                        valid--;
                    }
                    window.put(chartToRemove, window.get(chartToRemove) - 1);
                }
            }
        }

        String result;
        if (length < Integer.MAX_VALUE) {
            result = orig.substring(start, start + length);
        } else {
            result = StringUtils.EMPTY;
        }
        return result;
    }


    public static void main(String[] args) {
        System.out.println(getMinConstantsString("ADOBECODEBANC", "ABC"));
        System.out.println(getMinConstantsString("ADOBECODEBANC", "AB"));
    }
}
