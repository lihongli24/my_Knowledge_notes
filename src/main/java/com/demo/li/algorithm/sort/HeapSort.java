package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * 堆排序
 *
 * @author ：lihongli
 * @date ：Created in 2022/2/24 00:02
 */
public class HeapSort {

  private static void sort(int[] array) {

    // 构建堆
    for (int i = array.length - 1; i >= 0; i--) {
      heapify(array, i, array.length);
    }

    // 把最大的移动到最后面，然后这个位置不参与后面的流程
    int heapSize = array.length;
    swap(array, 0, --heapSize);

    // 因为0位置上的点是swap上来的，所以对他做下沉，完成后在新的0位置上的，放到最后的位置上，也就是之前的位置的前面一个
    while (heapSize > 0) {
      heapify(array, 0, heapSize);
      swap(array, 0, --heapSize);
    }

  }

  /**
   * 往下看，当前节点是否需要下沉
   *
   * @param array
   * @param i
   */
  private static void heapify(int[] array, int i, int heapSize) {
    int left = 2 * i + 1;
    while (left < heapSize) {
      // 选取出左右节点中较大的
      int largest = left + 1 < heapSize && array[left + 1] > array[left] ? left + 1 : left;
      largest = array[largest] > array[i] ? largest : i;
      // 如果下面没有比它大的了，停止下沉
      if (i == largest) {
        break;
      }
      // 下沉：把i位置的数据和largest交换，然后以largest开始和他下面的左右子节点比较
      swap(array, i, largest);
      i = largest;
      left = 2 * i + 1;
    }


  }

  private static void swap(int[] array, int i, int largest) {
    int tmp = array[i];
    array[i] = array[largest];
    array[largest] = tmp;

  }


  public static void main(String[] args) {
    int[] aa = new int[]{1, 6, 2, 4, 1, 2, 8, 3};
    sort(aa);
    Arrays.stream(aa).forEach(System.out::println);
  }

}
