package com.atguigu.e_array;

public class Demo05Test {
    //创建长度为 5 的 int 数组，存入随机 5 个整数，计算数组最大值并输出。
    static void main(String[] args) {
        int[] arr = {1,2,3,4,5};
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (max<arr[i]){
                max=arr[i];
            }
        }
        System.out.println(max);
    }
}
