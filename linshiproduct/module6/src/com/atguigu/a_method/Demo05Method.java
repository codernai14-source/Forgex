package com.atguigu.a_method;

public class Demo05Method {
    public static void main(String[] args) {
       int [] arr2 = method();
        for (int i = 0; i < arr2.length; i++) {
            System.out.println(arr2[i]);
        }
    }
    public static int[] method(){
        int a =10;
        int b =15;
        int sum =a+b;
        int sub =a-b;
        int [] arr ={sum,sub};
        return arr;
    }
}
