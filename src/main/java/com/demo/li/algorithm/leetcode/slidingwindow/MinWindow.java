package com.demo.li.algorithm.leetcode.slidingwindow;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * https://leetcode-cn.com/problems/minimum-window-substring/
 * <p>
 * Created by lihongli24 on 2020/5/30.
 */
public class MinWindow {

    public String minWindow(String s, String t) {
        char[] orgArray = s.toCharArray();
        char[] needArray = t.toCharArray();

        //将需要的填入needmap
        Map<Character, Integer> needMap = new HashMap<>();
        for (char needChar : needArray) {
            needMap.put(needChar, needMap.getOrDefault(needChar, 0) + 1);
        }

        int left = 0, right = 0;

        int valid = 0;

        int start = 0;
        int length = Integer.MAX_VALUE;
        Map<Character, Integer> window = new HashMap<>();
        while (right < orgArray.length) {
            char readyToAdd = orgArray[right];
            right++;

            if (needMap.containsKey(readyToAdd)) {
                window.put(readyToAdd, window.getOrDefault(readyToAdd, 0) + 1);
                if (window.get(readyToAdd).equals(needMap.get(readyToAdd))) {
                    valid++;
                }
            }

            while (valid == needMap.size()) {
                if (right - left < length) {
                    start = left;
                    length = right - left;
                }

                char readyToRemove = orgArray[left];
                left++;
                if (needMap.containsKey(readyToRemove)) {
                    if (window.get(readyToRemove).equals(needMap.get(readyToRemove))) {
                        valid--;
                    }
                    window.put(readyToRemove, window.get(readyToRemove) - 1);
                }
            }
        }

        return length < Integer.MAX_VALUE ? s.substring(start, start + length) : StringUtils.EMPTY;
    }

    public static void main(String[] args) {
        MinWindow minWindow = new MinWindow();
        System.out.println(minWindow.minWindow("aa", "aa"));
    }


}
