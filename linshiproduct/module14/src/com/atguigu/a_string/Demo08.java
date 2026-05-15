package com.atguigu.a_string;

import java.util.Scanner;

public class Demo08 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.next();
        int a=0;
        int A=0;
        int num=0;
        byte[] bytes=s.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i]>='a'&&bytes[i]<='z'){
                A++;
            }else if (bytes[i]>=97&&bytes[i]<=122){
                a++;
            }else if (bytes[i]>=48&&bytes[i]<=57){
                num++;
            }
        }
        System.out.println(A);
        System.out.println(a);
        System.out.println(num);
    }
}
