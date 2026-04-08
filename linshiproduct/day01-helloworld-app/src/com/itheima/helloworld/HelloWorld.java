package com.itheima.helloworld;

public class HelloWorld {
    static void main(String[] args) {
        System.out.println("HelloWorld!");
        System.out.println("--------------------");
        printHelloWorld();
        System.out.println("--------------------");
        System.out.println(sum(1, 2));

    }

    //打印三行HelloWorld
    static void printHelloWorld() {
        System.out.println("HelloWorld!");
        System.out.println("HelloWorld!");
        System.out.println("HelloWorld!");
    }
    //求任意两个整数的和
    static int sum(int a, int b) {
        return a + b;
    }

}