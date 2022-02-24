package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * 插入排序
 *
 * @author ：lihongli
 * @date ：Created in 2022/2/17 23:33
 */
public class InsertSort {

  public static void sort(int[] array) {
    if (null == array || array.length < 2) {
      return;
    }

    // 从1开始，往前比较，如果比前面的数字小，就交换
    // 直到 大于等于前面那个数字，或者前面没数字了
    for (int i = 1; i < array.length; i++) {
      for (int j = i - 1; j >= 0; j--) {
        if (array[j + 1] < array[j]) {
          swap(array, j, j + 1);
        }
      }
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
