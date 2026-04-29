package com.atguigu.a_staticutil;

public class Arraysutils {

    private Arraysutils(){

    }
    public static int getMax(int[] arr){
        int max =arr[0];
        arr[0]=max;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i]>max){
                max=arr[i];
            }
        }
        return max;
    }
}
