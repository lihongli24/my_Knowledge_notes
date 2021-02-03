package com.demo.li.algorithm.sort;

/**
 * @author lihongli
 * @date 2020/12/15 22:32
 */
public class BlueSortTest {

    private static void blueSort(int[] arr) {
        if (null == arr || arr.length == 1) {
            return;
        }

        for (int i = arr.length - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }
        }
    }

    public static void main(String[] args ){
        int[] arr = {4, 5,6, 1,2,3};
        blueSort(arr);
        for (int i : arr){
            System.out.println(i);
        }
    }
}
