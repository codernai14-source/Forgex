package com.atguigu.a_array;

public class Demo09ArrayException {
    static void main(String[] args) {
     int[] arr=new int[3];
        System.out.println(arr.length);
        arr=null;
        System.out.println(arr);
    }
}
