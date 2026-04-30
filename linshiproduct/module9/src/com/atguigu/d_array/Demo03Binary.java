package com.atguigu.d_array;

public class Demo03Binary {
    public static void main(String[] args) {
        int[] arr = {1, 2, 5, 6, 7, 8, 9};
        int result = binary(arr, 8);
        System.out.println(result);
    }

    public static int binary(int[] arr, int date) {
        int max = arr.length - 1;
        int min = 0;
        int mid = 0;
        while (max >= min) {
            mid = (max + min) / 2;
            if (date < arr[mid]) {
                max = mid - 1;
            } else if (date > arr[mid]) {
                min = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }
}

