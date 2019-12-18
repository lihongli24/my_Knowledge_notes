package algorithm.sort;

import java.util.Arrays;

/**
 * 冒泡排序
 */
public class BubbleSort {

    public static int[] sort(int[] input) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input.length - i - 1; j++) {
                if (input[j] > input[j + 1]) {
                    int vj = input[j];
                    input[j] = input[j + 1];
                    input[j + 1] = vj;
                }
            }
            System.out.println("第" + i + "轮，计算完的结果是" + Arrays.toString(input));
        }
        return input;
    }

    public static void main(String[] args) {
        int[] a = new int[]{4, 1, 3, 5, 2, 6, 1};
        a = sort(a);
        Arrays.stream(a).forEach(item -> {
            System.out.println(item + "");
        });
    }
}
