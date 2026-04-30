package com.atguigu.d_array;

public class Demo02Bubble {
    public static void main(String[] args) {
        int[] arr = {6,5,4,3,2,1};
        method(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

    public static void method(int[] arr){
        for (int j = 0; j < arr.length-1; j++) {
            for (int i = 0; i < arr.length-1-j; i++) {
              if (arr[i]>arr[i+1]){
                  int temp= arr[i];
                  arr[i]=arr[i+1];
                  arr[i+1]=temp;
                }
            }
        }
    }
}
