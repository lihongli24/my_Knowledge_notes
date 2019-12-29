package algorithm.leetcode;

import java.util.Arrays;

public class GoSteps {

    /**
     * 递归算法
     *
     * @param i 长度
     * @return
     */
    private static int recSteps(int i) {
        if (i <= 2) {
            return i;
        } else {
            return recSteps(i - 1) + recSteps(i - 2);
        }
    }


    /**
     * 动态规划算法
     *
     * @param length
     * @return
     */
    private static int dpSteps(int length) {
        int[] opt = new int[length + 1];
        for (int i = 0; i <= length; i++) {
            if (i <= 2) {
                opt[i] = i;
            } else {
                opt[i] = opt[i - 1] + opt[i - 2];
            }
        }
        return opt[length];
    }

    public static void main(String[] args) {
        System.out.println(dpSteps(10));
    }
}
