package com.atguigu.c_method;

public class Demo01Method {
    /*定义一个无参数、无返回值的方法，方法内部实现：控制台打印输出 3 行
     “Java 学习，天天进步”，并在 main 方法中调用该方法。
     */
    public static void main(String[] args) {
        print();
    }
    public static void print(){
        for (int i = 0; i <3 ; i++) {
            System.out.println("Java 学习，天天进步");
        }
    }
}
