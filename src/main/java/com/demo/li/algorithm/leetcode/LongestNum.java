package com.demo.li.algorithm.leetcode;

/**
 * 找出连续的.和小于k的最大长度
 */
public class LongestNum {
    private static int[] nums = {1, 2, 3, 1, 2, 1, 1, 1};


    private static int getLongest(int k) {
        int[][] numLength = new int[nums.length][2];
        int start = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i == 0) {
                numLength[0][0] = nums[0];
                numLength[0][1] = 1;
            } else if (nums[i] + numLength[i - 1][0] <= k) {
                numLength[i][0] = nums[i] + numLength[i - 1][0];
                numLength[i][1] = i - start +1;
            } else {
                numLength[i][0] = nums[i] + numLength[i - 1][0] - nums[start];
                start = start + 1;
                numLength[i][1] = i - start + 1;
            }
        }
        int max =0;
        for (int i = 0; i < nums.length; i++) {
            if(numLength[i][0] <= k){
                if(numLength[i][1] > max){
                    max = numLength[i][1];
                }
            }
        }

        return max;
    }

    public static void main(String[] args){
        System.out.println(getLongest(4));
    }

}
