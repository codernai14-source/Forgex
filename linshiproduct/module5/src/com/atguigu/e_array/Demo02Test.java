package com.atguigu.e_array;

public class Demo02Test {
    static void main(String[] args) {
        //手动创建一个长度为 4 的 int 数组，存入任意 4 个整数，求该数组中所有元素的总和与平均值。
        int[] arr = {11,22,33,44};
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        System.out.println(sum);
        double avg = sum/arr.length;
        System.out.println(avg);
    }
}
