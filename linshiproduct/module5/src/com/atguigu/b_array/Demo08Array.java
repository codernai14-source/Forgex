package com.atguigu.b_array;

public class Demo08Array {
    static void main(String[] args) {
        int[] arr1 = {1,2,3};
        int[] arr2 = {4,5,6};
        int[] newArr = new int[arr1.length+arr2.length];
        for (int i = 0; i < arr1.length; i++) {
            newArr[i]=arr1[i];
        }
        int aa =arr1.length;
        for (int i = 0; i < arr2.length; i++) {
            newArr[aa+i]=arr2[i];
        }
        for (int i = 0; i < newArr.length; i++) {
            System.out.println(newArr[i]);
        }
    }

}
