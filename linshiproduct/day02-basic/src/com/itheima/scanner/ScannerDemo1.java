package com.itheima.scanner;

import java.util.Scanner;

public class ScannerDemo1 {
    static void main(String[] args) {
        printUserInfo();

    }

    public static void printUserInfo() {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入名字");
        String username = sc.next();
        System.out.println("你名字"+username);
        System.out.println("您贵庚");
        int age = sc.nextInt();
        System.out.println("你"+age+'岁');

    }
}
