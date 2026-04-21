package com.atguigu.a_method;

import java.util.Scanner;

public class Demo03Method {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a= sc.nextInt();
        print(a);
    }
    public static void print(int a){
        int i =0;
        while (i<a){
            System.out.println("我是好人");
            i++;
        }
    }
}
