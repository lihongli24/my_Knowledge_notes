package com.demo.li.algorithm.sort;

import java.util.Arrays;

/**
 * @author lihongli
 * @date 2021/1/19 23:54
 */
public class MergeSort {

    public static void main(String[] args){
        int[] arr = {1,3, 5,6,2,3,3,1};
        process(arr, 0, arr.length - 1);
        System.out.println(Arrays.toString(arr));
    }

    private static void process(int[] arr, int L, int R) {
        if(L == R){
            return;
        }
        int mid = L + (R - L) / 2;
        process(arr, L, mid);
        process(arr, mid + 1, R);
        merge(arr, L, mid, R);
    }

    private static void merge(int[] arr, int l, int mid, int r) {
        int[] help = new int[r - l + 1];
        int lp = l;
        int rp = mid + 1;
        int helpIndex = 0;
        while (lp <= mid && rp <= r) {
            help[helpIndex++] = (arr[lp] <= arr[rp] ? arr[lp++] : arr[rp++]);
        }
        while (lp <= mid) {
            help[helpIndex++] = arr[lp++];
        }
        while (rp <= r) {
            help[helpIndex++] = arr[rp++];
        }
        for (int i = 0; i < help.length; i++) {
            arr[l + i] = help[i];
        }
    }
}
