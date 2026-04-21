package com.atguigu.a_method;

import java.util.Scanner;

public class Demo01Method {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int date = sc.nextInt();
        String result =print(date);
        System.out.println(result);

    }

    public static String print(int date ) {
        if (date%2==0){
            return "偶数";
        }else {
            return "奇数";
        }
    }
}
