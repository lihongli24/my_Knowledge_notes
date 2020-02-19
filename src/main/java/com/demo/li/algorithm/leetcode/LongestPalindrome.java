package com.demo.li.algorithm.leetcode;

/**
 * 最长回文
 */
public class LongestPalindrome {

    private static String getLongest(String str) {
        int max = 0;
        String result = "";
        String middle;
        for (int i = 0; i < str.length(); i++) {
            for (int j = i + 1; j <= str.length(); j++) {
                middle = str.substring(i, j);
                if (isPalindrome(middle) && middle.length() > max) {
                    max = middle.length();
                    result = middle;
                }
            }
        }
        return result;
    }

    /**
     * 检测是否是回文
     *
     * @param str
     * @return
     */
    private static boolean isPalindrome(String str) {
        for (int i = 0; i < str.length() / 2; i++) {
            if (str.charAt(i) != str.charAt(str.length() - i - 1)) {
                return false;
            }
        }
        return true;
    }


    public static void main(String[] args) {
        System.out.println(getLongest("bbnnxxxxnnbc"));
    }
}
