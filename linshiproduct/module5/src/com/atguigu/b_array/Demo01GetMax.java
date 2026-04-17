package com.atguigu.b_array;

public class Demo01GetMax {
    static void main(String[] args) {
        int[] arr = {5, 4, 5, 6, 6, 7, 8, 8, 9};
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (max < arr[i]) {
                max = arr[i];
            }
        }
        System.out.println(max);
    }
}



