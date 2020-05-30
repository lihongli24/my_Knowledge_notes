package com.demo.li.algorithm.leetcode.twopoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://leetcode-cn.com/problems/3sum/
 *
 * @author lihongli
 *         create：2020/5/29 10:47 下午
 */
public class ThreeNumAdd {

    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();

        for (int i = 0; i < nums.length - 2; i++) {
            int left = i + 1;
            int right = nums.length - 1;

            if(i > 0 && nums[i] == nums[i-1]){
                continue;
            }

            if (nums[i] > 0) {
                break;
            }

            while (left < right) {
                int tmp = nums[i] + nums[left] + nums[right];
                if (tmp == 0) {

                    System.out.println("i = " + i + ", lef = " + left + ", right = " + right);
                    List<Integer> list = new ArrayList<>();
                    list.add(nums[i]);
                    list.add(nums[left]);
                    list.add(nums[right]);
                    result.add(list);


                    while (left < right && nums[left] == nums[left + 1]) {
                        left ++;
                    }
                    while (left < right && nums[right] == nums[right] - 1){
                        right --;
                    }


                    left++;
                    right--;


                } else if (tmp < 0) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {

        ThreeNumAdd threeNumAdd = new ThreeNumAdd();

        int[] nums = new int[]{-1, 0, 1, 2, -1, -4};
        List<List<Integer>> threeSum = threeNumAdd.threeSum(nums);
        System.out.print(threeSum);

    }


}
