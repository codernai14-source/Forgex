package com.itheima.c_switch;

import java.util.Scanner;

public class SwitchDemo1 {
    static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入一个整数");
        int date = sc.nextInt();
        switch (date){
            case 1:
                System.out.println("星期一");
                break;
            case 2:
                System.out.println("星期二");
                break;
            default:
                System.out.println("嘻嘻");
                break;
        }
    }
    //byte,short,int,char 枚举类型 string类型。
}
