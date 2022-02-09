package com.demo.li.algorithm.sort;

/**
 * @author ：lihongli
 * @date ：Created in 2022/2/8 23:52
 */
public class 逆序对 {

  public static int process(int[] array, int l, int r) {
    if (l == r) {
      return 0;
    }

    int m = l + (r - l) / 2;

    return process(array, l, m) + process(array, m + 1, r) + merge(array, l, m, r);
  }

  private static int merge(int[] array, int l, int m, int r) {
    int[] help = new int[r - l + 1];
    int p1 = l;
    int p2 = m + 1;
    int i = 0;

    int count = 0;

    while (p1 <= m && p2 <= r) {
      count += array[p1] > array[p2] ? (r - p2 + 1) : 0;
      help[i++] = array[p1] <= array[p2] ? array[p1++] : array[p2++];
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

    return 0;
  }

}
