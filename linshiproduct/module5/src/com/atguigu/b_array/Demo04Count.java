package com.atguigu.b_array;

import java.util.Random;

public class Demo04Count {
    static void main(String[] args) {
        Random ra = new Random();
        int[] arr = new int[50];
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
           arr[i] = ra.nextInt(100)+1;
           if (arr[i]%2==0){
               count++;
           }
        }
        System.out.println(count);
    }
}
