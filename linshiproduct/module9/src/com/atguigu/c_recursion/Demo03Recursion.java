package com.atguigu.c_recursion;

public class Demo03Recursion {
    public static void main(String[] args) {
       int sum = method(4);
        System.out.println(sum);

    }


    public static int method(int n){
        if (n==1){
            return 1;
        }
           return n*method(n-1);


    }
}
