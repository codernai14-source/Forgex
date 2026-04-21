package com.atguigu.c_method;

public class Demo02Method {
    //定义一个方法，接收一个 int 数组作为参数，遍历打印数组中的所有元素；在 main 方法中创建数组并调用该方法。
    public static void main(String[] args) {
        int[] b={1,2,3,3};
        print(b);
    }
    public static void print(int a[]){
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
    }
}
