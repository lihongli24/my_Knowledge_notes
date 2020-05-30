package com.demo.li.algorithm.leetcode;

/**
 * https://leetcode-cn.com/problems/house-robber/
 * <p>
 * Created by lihongli24 on 2020/5/29.
 */
public class HouseRobber {

    public int rob(int[] nums) {

        if(nums.length == 0){
            return 0;
        }

        if(nums.length < 2){
            return nums[0];
        }
        int[] values = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            if (i == 0) {
                values[0] = nums[0];
            } else if (i == 1) {
                values[1] = Math.max(values[0], nums[1]);
            } else {
                values[i] = Math.max(nums[i] + values[i - 2], values[i - 1]);
            }
        }

        return Math.max(values[nums.length - 1], values[nums.length - 2]);

    }

    public static void main(String[] args) {
        HouseRobber houseRobber = new HouseRobber();
        System.out.println(houseRobber.rob(new int[]{1, 2}));
    }


}
