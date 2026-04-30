package com.atguigu.c_recursion;

public class Demo04Recursion {
    public static void main(String[] args) {
        int result = method(3);
        System.out.println(result);
    }

    public static int method(int n){
        if (n==1|n==2){
            return 1;
        }return method(n-2)+method(n-1);
    }
}
