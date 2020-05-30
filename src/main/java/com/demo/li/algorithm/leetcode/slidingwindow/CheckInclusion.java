package com.demo.li.algorithm.leetcode.slidingwindow;

import java.util.HashMap;
import java.util.Map;

/**
 * https://leetcode-cn.com/problems/permutation-in-string/
 * <p>
 * Created by lihongli24 on 2020/5/30.
 */
public class CheckInclusion {
    public boolean checkInclusion(String s1, String s2) {
        char[] origArray = s2.toCharArray();
        char[] needArray = s1.toCharArray();

        Map<Character, Integer> needMap = new HashMap<>();
        for (char needChar : needArray) {
            needMap.put(needChar, needMap.getOrDefault(needChar, 0) + 1);
        }

        int left = 0, right = 0;
        int valid = 0;
        Map<Character, Integer> window = new HashMap<>();
        while (right < s2.length()) {
            char readyToAdd = origArray[right];
            right++;
            if (needMap.containsKey(readyToAdd)) {
                window.put(readyToAdd, window.getOrDefault(readyToAdd, 0) + 1);
                if (window.get(readyToAdd).equals(needMap.get(readyToAdd))) {
                    valid++;
                }
            }

            while (right - left >= s1.length()) {

                //不存在 找ad,出现acd的情况，因为ac的时候就已经被划出去了
                if (valid == needMap.size()) {
                    return true;
                }

                char readyToRemove = origArray[left];
                left++;

                if (needMap.containsKey(readyToRemove)) {
                    if (needMap.get(readyToRemove).equals(window.get(readyToRemove))) {
                        valid--;
                    }
                    window.put(readyToRemove, window.get(readyToRemove) - 1);
                }

            }
        }

        return false;
    }

    public static void main(String[] args) {
        CheckInclusion checkInclusion = new CheckInclusion();
        System.out.println(checkInclusion.checkInclusion("ab", "eidbaooo"));
        System.out.println(checkInclusion.checkInclusion("ab", "eidboaoo"));
    }


}
