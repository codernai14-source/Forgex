package com.atguigu.d_array;

public class Demo01Reverse {
    public static void main(String[] args) {
        int[] arr = {6,5,4,3,2,1};
        method(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

    public static void method(int[] arr){
        for (int min=0,max= arr.length-1;max>min;max--,min++){
            int temp = arr[min];
            arr[min] = arr[max];
            arr[max] = temp;
        }
    }
}
