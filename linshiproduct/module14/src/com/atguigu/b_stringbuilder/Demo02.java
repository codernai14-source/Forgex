package com.atguigu.b_stringbuilder;

import java.util.Scanner;

public class Demo02 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String date = sc.next();
        StringBuilder sb = new StringBuilder(date);
        sb.reverse();
        String s = sb.toString();
        if (date.equals(s)){
            System.out.println("yes huiwen");
        }else{
            System.out.println("no huiwen");
        }
    }
}
