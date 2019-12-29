package algorithm.leetcode;

/**
 * 01背包问题
 * https://yq.aliyun.com/articles/714964
 */
public class BagProblem {

    private static int[] w = new int[]{0, 2, 2, 6, 5, 4};
    private static int[] p = new int[]{0, 6, 3, 5, 4, 6};

    /**
     * 递归算法
     *
     * @param i
     * @param j
     * @return
     */
    private static int recBag(int i, int j) {
        if (i == 0) {
            return 0;
        } else if (w[i] > j) {
            return recBag(i - 1, j);
        } else {
            return Math.max(recBag(i - 1, j), recBag(i - 1, j - w[i]) + p[i]);
        }
    }

    public static void main(String[] args) {
        System.out.println(recBag(w.length - 1, 10));
    }
}
