package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * @author ：lihongli
 * @date ：Created in 2022/2/18 22:37
 */
public class MergeSort2 {

  public static void process(int[] array, int L, int R) {
    if (L == R) {
      return;
    }
    int M = L + (R - L) / 2;
    process(array, L, M);
    process(array, M + 1, R);
    merge(array, L, M, R);
  }

  private static void merge(int[] array, int l, int m, int r) {
    int[] help = new int[r - l + 1];
    int i = 0;
    int p1 = l;
    int p2 = m + 1;
    while (p1 <= m && p2 <= r) {
      help[i++] = (array[p1] <= array[p2] ? array[p1++] : array[p2++]);
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
  }

  public static void main(String[] args) {
    int[] aa = new int[]{1, 6, 2, 4, 1, 2, 8, 3};
    process(aa, 0, aa.length -1);
    Arrays.stream(aa).forEach(System.out::println);
  }

}
