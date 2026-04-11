package com.itheima.type;

public class TypeDemo2 {
    static void main(String[] args) {
        System.out.println(calc(1,(byte)1,1,1.1));
    }
    public static double calc(int a,byte b,float c,double d) {
        return a + b + c + d;
    }
}
