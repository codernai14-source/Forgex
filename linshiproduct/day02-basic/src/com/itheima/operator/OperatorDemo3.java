package com.itheima.operator;

public class OperatorDemo3 {
    static void main(String[] args) {
        receive(90);
        pay(190);

    }
    public static void receive(int b) {
        int a = 100;
        a+=b;
        System.out.println("余额："+a);


    }
    public static void pay(int d) {
        int c = 190;
        c-=d;
        System.out.println("余额："+c);
    }
}
