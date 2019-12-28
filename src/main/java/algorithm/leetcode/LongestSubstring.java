package algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * https://leetcode-cn.com/problems/longest-substring-without-repeating-characters/solution/hua-dong-chuang-kou-by-powcai/
 * 给定一个字符串，请你找出其中不含有重复字符的 最长子串 的长度。
 */
public class LongestSubstring {
    public static int lengthOfLongestSubstring(String s) {
        int left = 0;
        int max = 0;
        Map<Character, Integer> cache = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            if (cache.get(s.charAt(i)) != null) {
                left = Math.max(left, cache.get(s.charAt(i)) + 1);
            }
            cache.put(s.charAt(i), i);
            max = Math.max(max, i - left +1);
        }
        return max;
    }

    public static void main(String[] args){
        System.out.println(lengthOfLongestSubstring("abdcbacdefcsf"));
    }
}
