package com.itheima.variable;

public class Variable {
    public static void main(String[] args) {
        printVariable();
    }
    //定义一个方法，来学习变量的定义
    public static void printVariable() {
        //定义变量：数据类型 变量名 = 初始值
        int age = 10;
        System.out.println(age);
        //定义一个小数变量，存储学生Java成绩
        double score = 89.5;
        System.out.println(score);

        System.out.println("--------------------");
        //为什么要创建变量？


        System.out.println("==========================");

        //变量数据可替换
        int age2=18;
        age2=20;
        System.out.println(age2);

        age2=age2+1;
        System.out.println(age2);

        System.out.println("==========================");
        //需求：微信钱包9.9，发出去了5元，又收到了6元，请求计算余额
        double balance = 9.9;
        balance = balance - 5 + 6;
        System.out.println(balance);
    }
}
