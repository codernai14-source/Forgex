package com.atguigu.b_utils;

import java.util.Arrays;

public class Demo02Arrays {
    public static void main(String[] args) {
        int[] arr={2,4,3,7,5};
        String s = Arrays.toString(arr);
        System.out.println(s);
        Arrays.sort(arr);
        System.out.println(Arrays.toString(arr));
        int i = Arrays.binarySearch(arr, 7);
        System.out.println(i);
        int[] ints = Arrays.copyOf(arr, 10);
        System.out.println(Arrays.toString(ints));
    }
}
