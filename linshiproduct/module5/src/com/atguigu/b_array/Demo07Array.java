package com.atguigu.b_array;

public class Demo07Array {
    static void main(String[] args) {
        int[] arr1={1,2,3,4,5};
        int[] arr2=new int[10];
        for (int i = 0; i < arr1.length; i++) {
            arr2[i]=arr1[i];

        }
        arr2=arr1;
        for (int i = 0; i < arr2.length; i++) {
            System.out.println(arr2[i]);
        }
        System.out.println(arr1);
        System.out.println(arr2);
    }
}
