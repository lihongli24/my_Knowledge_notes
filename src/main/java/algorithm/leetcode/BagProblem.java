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

    /**
     *  动态规划的解法
     * @param total
     * @return
     */
    private static int dpBag(int total) {
        int[][] fmap = new int[w.length][total + 1];
        for (int i = 0; i < w.length; i++) {
            for (int j = 0; j <= total; j++) {
                if (i == 0) {
                    fmap[i][j] = 0;
                } else if (w[i] > j) {
                    fmap[i][j] = fmap[i - 1][j];
                } else {
                    int a = fmap[i - 1][j];
                    int b = fmap[i - 1][j - w[i]] + p[i];
                    fmap[i][j] = Math.max(a, b);
                }
            }
        }
        return fmap[w.length - 1][total];
    }

    public static void main(String[] args) {
//        System.out.println(recBag(w.length - 1, 10));
        System.out.println(dpBag(10));
    }
}
