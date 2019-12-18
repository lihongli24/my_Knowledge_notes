package algorithm.search;

public class BinarySearch {
    private static int search(int[] arrays, int num) {
        int start = 0;
        int end = arrays.length - 1;
        int middle = (end - start) / 2;
        while (start <= end) {
            if (arrays[middle] > num) {
                end = middle - 1;
            } else if (arrays[middle] < num) {
                start = middle + 1;
            } else {
                return arrays[middle];
            }
        }
        throw new RuntimeException("can't get");

    }

    public static void main(String[] args){
        int[] a = new int[]{1,2,3,4,5,6,7};
        int result = BinarySearch.search(a, 4);
        System.out.println("result" + result);

    }
}
