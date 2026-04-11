package com.itheima.operator;

public class OperatorDemo5 {
    static void main(String[] args) {
        max(3,4);
        max(3,3);
        print(59);
        print2(1,2,2);
    }
    //比较大小
    public static void max(int a,int b) {
        int max =a>b?a:b;
        System.out.println("max:"+max);
    }
    //判断是否挂科
    public static void print(int score) {
        String grade =score>=60?"pass":"fail";
        System.out.println(grade);
    }
    public static void print2(int a,int b,int c) {
        int max2 =a>b?a>c?a:c:b>c?b:c;
        System.out.println(max2);
    }
}
