package com.demo.li.algorithm.leetcode.slidingwindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://leetcode-cn.com/problems/find-all-anagrams-in-a-string/
 * <p>
 * Created by lihongli24 on 2020/5/30.
 */
public class FindAnagrams {

    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> starts = new ArrayList<>();
        if (null == s || s.length() < p.length()) {
            return starts;
        }

        char[] origArray = s.toCharArray();
        char[] needArray = p.toCharArray();

        Map<Character, Integer> needMap = new HashMap<>();
        for (char needChar : needArray) {
            needMap.put(needChar, needMap.getOrDefault(needChar, 0) + 1);
        }

        int left = 0, right = 0;
        int valid = 0;
        Map<Character, Integer> window = new HashMap<>();
        while (right < s.length()) {
            char readyToAdd = origArray[right];
            right++;

            if (needMap.containsKey(readyToAdd)) {
                window.put(readyToAdd, window.getOrDefault(readyToAdd, 0) + 1);
                if (window.get(readyToAdd).equals(needMap.get(readyToAdd))) {
                    valid++;
                }
            }


            while (right - left >= p.length()) {
                if (valid == needMap.size()) {
                    starts.add(left);
                }

                char readyToRemove = origArray[left];
                left++;
                if (needMap.containsKey(readyToRemove)) {
                    if (window.get(readyToRemove).equals(needMap.get(readyToRemove))) {
                        valid--;
                    }
                    window.put(readyToRemove, window.get(readyToRemove) - 1);
                }
            }
        }

        return starts;
    }

    public static void main(String[] args) {
//        s: "cbaebabacd" p: "abc"
        FindAnagrams findAnagrams = new FindAnagrams();
        System.out.println(findAnagrams.findAnagrams("abab", "ab"));
    }

}
