package com.itheima.method;

public class Methoddemo1 {
    static void main(String[] args) {
        //目标：掌握方法定义和调用
        int sum = getSum(10,20);
        System.out.println("和是："+sum);

        int sum2 = getSum(20,30);
        System.out.println("和是："+sum2);
        System.out.println();
        // int 变量b
        Person person = new Person();
        person.name="hqj";
        System.out.println(person.name);
    }

    //定义一个方法，求任意两个整数的合并返回
    public static int getSum(int a,int b) {
        a=a+1;
        b= b-1;
        return a+b;
    }
    //打印三行HelloWorld
    public static void printHelloWorld() {
        System.out.println("HelloWorld");
        System.out.println("HelloWorld");
        System.out.println("HelloWorld");
    }

}
