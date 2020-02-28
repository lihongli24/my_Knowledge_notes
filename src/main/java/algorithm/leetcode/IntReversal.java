package algorithm.leetcode;

/**
 * 数字反转
 *
 * @see {https://leetcode-cn.com/problems/reverse-integer/solution/hua-jie-suan-fa-7-zheng-shu-fan-zhuan-by-guanpengc/}
 */
public class IntReversal {

    /**
     * 将数字反转
     *
     * @param x 输入
     * @return
     */
    private static int doReversal(int x) {
        int result = 0;
        int current;

        while (x != 0) {
            current = x % 10;
            if (result > Integer.MAX_VALUE / 10 || (result == Integer.MAX_VALUE / 10 && current > 8)) {
                return 0;
            }
            if (result < Integer.MIN_VALUE / 10 || (result == Integer.MIN_VALUE / 10 && current < -7)) {
                return 0;
            }
            result = result * 10 + current;
            x = x / 10;
        }
        return result;
    }

    public static void main(String[] args) {
//        int input = -2147483648;
        int input = 1534236469;
        int result = doReversal(input);
        System.out.println(result);
    }
}
