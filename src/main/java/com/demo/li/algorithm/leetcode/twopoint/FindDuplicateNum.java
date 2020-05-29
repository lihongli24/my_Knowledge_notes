package com.demo.li.algorithm.leetcode.twopoint;

/**
 * https://leetcode-cn.com/problems/find-the-duplicate-number/
 *
 * 参考题解：https://leetcode-cn.com/problems/find-the-duplicate-number/solution/287xun-zhao-zhong-fu-shu-by-kirsche/
 *
 * @author lihongli
 * create：2020/5/28 11:41 下午
 */
public class FindDuplicateNum {

    public int findDuplicate(int[] nums) {
        int slow = nums[0];
        int fast = nums[nums[0]];


        //确认列表中出现了环
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[nums[fast]];
        }


        //找出环的起点
        fast = 0;
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }

        return slow;
    }

    public static void main(String[] args) {
        FindDuplicateNum findDuplicateNum = new FindDuplicateNum();
        System.out.println(findDuplicateNum.findDuplicate(new int[]{3,1,3, 4,2}));

    }

}
