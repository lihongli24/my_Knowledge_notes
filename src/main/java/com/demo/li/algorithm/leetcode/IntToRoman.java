package com.demo.li.algorithm.leetcode;

/**
 * https://leetcode-cn.com/problems/integer-to-roman/
 * 整型转化罗马数字
 *
 * @author lihongli
 * create：2020/1/28 12:16 上午
 */
public class IntToRoman {
    /**
     * I             1
     * V             5
     * X             10
     * L             50
     * C             100
     * D             500
     * M             1000
     * <p>
     * 来源：力扣（LeetCode）
     * 链接：https://leetcode-cn.com/problems/integer-to-roman
     * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
     */
    private static final int[] nums = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] romans = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private static String intToRoman(int input) {
        StringBuilder result = new StringBuilder("");
        int index = 0;
        while (input >=0 && index < nums.length){

            if(input - nums[index] < 0){
                index = index + 1;
                continue;
            }
            result.append(romans[index]);
            input = input - nums[index];
        }
        return result.toString();
    }

    public static void main(String[] args){
        int input = 1994;
        System.out.println(intToRoman(input));
    }
}
