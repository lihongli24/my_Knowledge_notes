package com.demo.li.algorithm.sort;

/**
 * @author ：lihongli
 * @date ：Created in 2022/2/8 22:55
 * <p>
 * 描述：在一个数组中，每一个元素左边比当前元素值小的元素值累加起来，叫做这个数组的小和
 * <p>
 * 例如：[2,3,4,1,5]
 * <p>
 * 2左边比2小的元素：无
 * <p>
 * 3左边比3小的元素：2
 * <p>
 * 4左边比4小的元素：2，3
 * <p>
 * 1左边比1小的元素：无
 * <p>
 * 5左边比5小的元素：2,3,4,1
 * <p>
 * 小和small_sum = 2 + 2 + 3 + 2 + 3 + 4 + 1 = 17
 */
public class 小和问题 {

  public static void main(String[] args) {
    int[] aa = {2,3,4,1,5};
    System.out.println(process(aa, 0, aa.length -1));
  }

  public static int process(int[] array, int l, int r) {
    if (l == r) {
      return 0;
    }

    int m = l + (r - l) / 2;

    return process(array, l, m)
        + process(array, m + 1, r)
        + merge(array, l, r, m);
  }

  private static int merge(int[] array, int l, int r, int m) {
    int p1 = l;
    int p2 = m + 1;
    int[] help = new int[r - l + 1];
    int i = 0;
    int smallSum = 0;

    while (p1 <= m && p2 <= r) {
      smallSum += array[p1] < array[p2] ? (r - p2 + 1) * array[p1] : 0;
      help[i++] = (array[p1] < array[p2] ? array[p1++] : array[p2++]);
    }
    while (p1 <= m) {
      help[i++] = array[p1++];
    }
    while (p2 <= r) {
      help[i++] = array[p2++];
    }

    for (int j = 0; j < help.length; j++) {
      array[l + j] = help[j];
    }
    return smallSum;
  }


}
