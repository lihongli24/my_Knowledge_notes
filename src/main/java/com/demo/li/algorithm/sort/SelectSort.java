package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * 选择排序
 *
 * @author ：lihongli
 * @date ：Created in 2022/2/17 00:06
 */
public class SelectSort {

  public static void sort(int[] array) {
    if (null == array || array.length < 2) {
      return;
    }

    // 0 ~ n-1 选择最小的，放在0上
    // 1 ~ n-1 选择最小的。放在1上
    // ....
    for (int i = 0; i < array.length - 1; i++) { // i ~ n-1上寻找最小的值，
      int minIndex = i;
      for (int j = i + 1; j < array.length; j++) {
        if (array[minIndex] > array[j]) {
          minIndex = j;
        }
      }
      swap(array, i, minIndex);
    }
  }
  private static void swap(int[] array, int i, int j) {
    int help = array[i];
    array[i] = array[j];
    array[j] = help;
  }
  public static void main(String[] args) {
    int[] aa = new int[]{1, 6, 2, 4, 1, 2, 8, 3};
    sort(aa);
    Arrays.stream(aa).forEach(System.out::println);
  }
}
