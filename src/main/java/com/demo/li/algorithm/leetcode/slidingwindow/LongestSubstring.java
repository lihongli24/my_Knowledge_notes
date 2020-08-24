package com.demo.li.algorithm.leetcode.slidingwindow;

import org.testng.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lihongli24 on 2020/5/30.
 */
public class LongestSubstring {

    public int lengthOfLongestSubstring(String s) {
        char[] chars = s.toCharArray();

        int left = 0;
        int right = 0;
        Map<Character, Integer> window = new HashMap<>();
        int size = 0;
        while (right < s.length()) {
            char current = chars[right];
            right++;
            window.put(current, window.getOrDefault(current, 0) + 1);

            while (window.get(current) > 1) {
                char remove = chars[left];
                left++;

                window.put(remove, window.get(remove) - 1);
            }

            size = right - left > size ? right - left : size;
        }

        return size;
    }

    public static void main(String[] args){
        LongestSubstring longestSubstring = new LongestSubstring();
        Assert.assertEquals(3,longestSubstring.lengthOfLongestSubstring("abcabcbb") );
        Assert.assertEquals(1,longestSubstring.lengthOfLongestSubstring("bbbbb") );
        Assert.assertEquals(3,longestSubstring.lengthOfLongestSubstring("pwwkew") );
    }
}
