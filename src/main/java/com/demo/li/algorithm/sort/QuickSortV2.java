package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * 快拍
 *
 * @author ：lihongli
 * @date ：Created in 2022/2/23 22:42
 */
public class QuickSortV2 {

  private static void process2(int[] array, int l, int r) {
    if (l >= r) {
      return;
    }

    final int[] m = partition(array, l, r);
    process2(array, l, m[0] - 1);
    process2(array, m[1] + 1, r);
  }

  private static int[] partition(int[] array, int l, int r) {
    if (l > r) {
      return new int[]{-1, -1};
    }
    if (l == r) {
      return new int[]{l, r};
    }

    int less = l - 1;
    int more = r;
    int index = l;
    while (index < more) {
      if (array[index] < array[r]) {
        swap(array, ++less, index++);
      } else if (array[index] == array[r]) {
        index++;
      } else {
        swap(array, --more, index);
      }
    }

    swap(array, r, more);
    return new int[]{less + 1, more};
  }

  private static void swap(int[] array, int x, int y) {
    int help = array[x];
    array[x] = array[y];
    array[y] = help;
  }

  public static void main(String[] args) {
    int[] aa = new int[]{1, 6, 2, 4, 1, 2, 8, 3};
    process2(aa, 0, aa.length -1);
    Arrays.stream(aa).forEach(System.out::println);
  }


}
