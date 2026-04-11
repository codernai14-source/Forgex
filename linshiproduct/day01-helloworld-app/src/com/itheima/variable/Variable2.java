package com.itheima.variable;

public class Variable2 {
    static void main(String[] args) {
        //目标：掌握八种基本数据类型定义变量
        printVariable();
    }
    //请帮我设计一个方法，打印出8种基本数据类型定义的变量
    public static void printVariable() {
        byte b = 10;
        short s = 10;
        int i = 10;
        long l = 10;

        float f = 10.1f;
        double d = 10.1;

        char c = 'a';

        boolean bool = true;

        //5字符串型，定义字符串变量记住字符串数据的
        String str = "hello world";
        System.out.println(str);

    }
}
