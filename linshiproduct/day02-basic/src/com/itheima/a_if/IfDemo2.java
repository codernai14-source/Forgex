package com.itheima.a_if;

import java.util.Scanner;

public class IfDemo2 {
    static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        System.out.println("请输入一个数字");
        int date = sc.nextInt();
        if(date==1){
            System.out.println("星期一");
        }else if (date==2) {
            System.out.println("星期二");
        }else if (date==3) {
            System.out.println("星期三");
        }else if (date==4) {
            System.out.println("星期四");
        }else if (date==5) {
            System.out.println("星期五");
        }else if (date==6) {
            System.out.println("星期六");
        }else if (date==7) {
            System.out.println("星期日");
        }else
            System.out.println("数字有误");
    }
}
