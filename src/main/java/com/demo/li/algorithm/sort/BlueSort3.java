package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * 冒泡排序
 *
 * @author ：lihongli
 * @date ：Created in 2022/2/17 08:10
 */
public class BlueSort3 {

  public static void sort(int[] array) {
    if(null == array || array.length < 2){
      return;
    }
    // 0 ~ n-1 依次比较，大的往后放
    // 0 ~ n-2 依次比较，大的往后放
    // 0 ~ n-3 依次比较，大的往后放
    // ...
    for (int i = array.length - 1; i > 0; i--) { // 0 ~ i 上一次比较
      for (int j = 0; j < i; j++) {
        if (array[j] > array[j + 1]) {
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
