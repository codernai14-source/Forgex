package com.itheima.operator;

public class OperatorDemo2 {
    static void main(String[] args) {
        print(6);
        print2(9);
    }
    public static void print(int a) {
        a++;
        ++a;
        System.out.println(a);
        a--;
        --a;
        System.out.println(a);
        a++;
        System.out.println(a);


    }
    public static void print2(int a){
        int b = a++;
        System.out.println(a);
        System.out.println(b);

        int c = ++a;
        System.out.println(a);
        System.out.println(c);
    }

}
