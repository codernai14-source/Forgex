package com.itheima.a_if;

import java.util.Scanner;

public class IfDemo1 {
    static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int year=sc.nextInt();
        if ((year%4==0&&year%100!=0)||(year%400==0)){
            System.out.println("润年");
        }else{
            System.out.println("平年");
        }

    }
}
