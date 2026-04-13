package com.itheima.method;

public class lx {
    static void main(String[] args) {
        System.out.println("和是"+print(2,3));
        print2(3,6);
        print3(1.1f);

    }
    public static int print(int a,int b){
        return a+b;
    }

    public static void print2(int c,int d) {
        System.out.println(c+d);

    }
    public static void print3(float f) {
        System.out.println(f);

    }

}
