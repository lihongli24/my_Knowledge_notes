package algorithm.leetcode;

import java.util.Arrays;

public class GoSteps {

    /**
     * 递归算法
     *
     * @param length 长度
     * @return
     */
    private static int recSteps(int length) {
        if (length == 1) {
            return 1;
        } else if (length == 2) {
            return 2;
        } else {
            return recSteps(length - 1) + recSteps(length - 2);
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
            if (i < 1) {
                opt[i] = 0;
            } else if (i == 1) {
                opt[i] = 1;
            } else if (i == 2) {
                opt[i] = 2;
            } else {
                opt[i] = opt[i - 1] + opt[i - 2];
            }
        }
        System.out.println(Arrays.toString(opt));
        return opt[length];
    }

    public static void main(String[] args) {
        System.out.println(dpSteps(10));
    }
}
