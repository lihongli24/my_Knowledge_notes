package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * 快拍
 *
 * @author ：lihongli
 * @date ：Created in 2022/2/23 22:42
 */
public class QuickSort {

  public static void process(int[] array, int l, int r) {

    if(l >= r){
      return;
    }

    final int m = partition(array, l, r);
    process(array, l, m - 1);
    process(array, m + 1, r);
  }

  public static int partition(int[] array, int l, int r) {
    if (l > r) {
      return -1;
    }
    if (l == r) {
      return l;
    }
    int less = l - 1;
    int index = l;

    while (index < r) {
      // 如果当前数字 <= 需要比较的数字，把它换到less的下一个位置，把less往后扩大一个
      if (array[index] <= array[r]) {
        swap(array, index, ++less);
      }
      index++;
    }

    // 把用来比较的数字换到less返回中：换到less的下一个位置,less往后扩大一个
    swap(array, r, ++less);
    return less;
  }

  private static void swap(int[] array, int x, int y) {
    int help = array[x];
    array[x] = array[y];
    array[y] = help;
  }

  public static void main(String[] args) {
    int[] aa = new int[]{1, 6, 2, 4, 1, 2, 8, 3};
    process(aa, 0, aa.length -1);
    Arrays.stream(aa).forEach(System.out::println);
  }

}
