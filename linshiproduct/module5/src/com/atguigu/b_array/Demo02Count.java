package com.atguigu.b_array;

import java.util.Random;

public class Demo02Count {
    static void main(String[] args) {
        int[] arr =new int[10];
        Random ra=new Random();
        int count=0;
        for (int i = 0; i < arr.length; i++) {
            arr[i]= ra.nextInt(101);
            if (arr[i]%3==0&&arr[i]%5==0&&arr[i]%7!=0){
                count++;
            }
        }
        System.out.println(count);
    }
}
