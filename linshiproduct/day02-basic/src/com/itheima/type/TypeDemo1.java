package com.itheima.type;

public class TypeDemo1 {
    static void main(String[] args) {
        byte b =1;
        print(b);
        int d =999;
        byte e = (byte) d;
        print2(e);
        double g = 6.66;
        int h = (int)g;
        print(h);

    }
    public static void print(int a){
        System.out.println(a);
    }
    public static void print2(byte c) {
        System.out.println(c);

    }
    public static void print3(double f) {
        System.out.println(f);
    }
}
