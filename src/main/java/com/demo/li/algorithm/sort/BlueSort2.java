package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * @author ：lihongli
 * @date ：Created in 2022/2/16 23:42
 */
public class BlueSort2 {

  public static void sort(int[] array) {
    // 0 ~ n-1
    // 0 ~ n-2
    // ...
    // 相邻两个数字比较，把大的往后方
    for (int i = 0; i < array.length - 1; i++) {
      for (int j = 0; j < array.length - 1 - i; j++) {
        if(array[j] > array[j+1]){
          swap(array, j , j+ 1);
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
