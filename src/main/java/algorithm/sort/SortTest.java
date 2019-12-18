package algorithm.sort;

import java.util.Arrays;

/**
 * 排序算法
 *
 * @link https://www.cnblogs.com/onepixel/p/7674659.html
 */
public class SortTest {

    /**
     * 冒泡排序
     *
     * @param input
     * @return
     */
    private static int[] blueSort(int[] input) {
        for (int i = 0; i < input.length - 1; i++) {
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

    /**
     * 选择排序
     *
     * @param input
     * @return
     */
    private static int[] selectionSort(int[] input) {

        for (int i = 0; i < input.length - 1; i++) {
            //i的位置定义为最小值
            int minIndex = i;

            //在一轮中寻找小于i的位置，然后把它交换过去
            for (int j = i + 1; j < input.length; j++) {
                if (input[j] < input[minIndex]) {
                    minIndex = j;
                }
            }

            //如果本轮中最小的不是i位置的，进行交换
            if (i != minIndex) {
                int tmp = input[i];
                input[i] = input[minIndex];
                input[minIndex] = tmp;
            }
        }
        return input;
    }

    /**
     * 插入排序
     *
     * @param input
     * @return
     */
    private static int[] insertionSort(int[] input) {
        //每一个向前比较，如果前面一个大于它，那就前面那个往后移动一位，直到查找到比它小的，或者查到了头，把它插入进去
        for (int i = 1; i < input.length; i++) {
            int preIndex = i - 1;
            int current = input[i];
            while (preIndex >= 0 && input[preIndex] > current) {
                input[preIndex + 1] = input[preIndex];
                preIndex--;
            }
            input[preIndex + 1] = current;
        }
        return input;
    }

    /**
     * 归并排序
     * @param input
     * @return
     */
    private static int[] mergeSort(int[] input) {
        int length = input.length;
        if (length < 2) {
            return input;
        }
        //把数据分为左右两堆，
        int middle = length / 2;
        int[] left = Arrays.copyOfRange(input, 0, middle);
        int[] right = Arrays.copyOfRange(input, middle, length);
        //分别做归并排序，最后做合并
        left = mergeSort(left);
        right = mergeSort(right);
        return merge(left, right);

    }

    private static int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int leftIndex = 0;
        int rightIndex = 0;
        //合并的时候，顺序判断左右两边的数字。一次添加到新的数组中。
        while (leftIndex < left.length && rightIndex < right.length) {
            if (left[leftIndex] <= right[rightIndex]) {
                result[leftIndex + rightIndex] = left[leftIndex];
                leftIndex++;
            } else {
                result[leftIndex + rightIndex] = right[rightIndex];
                rightIndex++;
            }
        }

        while (leftIndex < left.length) {
            result[leftIndex + rightIndex] = left[leftIndex];
            leftIndex++;
        }
        while (rightIndex < right.length) {
            result[leftIndex + rightIndex] = right[rightIndex];
            rightIndex++;
        }
        return result;
    }

    public static void main(String[] args) {
        int[] a = new int[]{4, 1, 3, 5, 2, 6, 1};
//        a = blueSort(a);
//        a = selectionSort(a);
//        a = insertionSort(a);
        a = mergeSort(a);
        System.out.println(Arrays.toString(a));
    }
}
