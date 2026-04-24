package com.atguigu.c_test;

public class TestCalculator {
    public static void main(String[] args) {
        new Calculator();
        System.out.println(new Calculator().add(10,20));

        printSum(new Calculator());
    }

    public static void printSum(Calculator a){
        System.out.println(a.add(30,40));


    }
}
