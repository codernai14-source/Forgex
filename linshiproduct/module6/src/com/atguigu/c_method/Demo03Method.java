package com.atguigu.c_method;

public class Demo03Method {
    //定义两个重载的add方法：
    //第一个：接收 2 个 int 参数，返回两数之和
    //第二个：接收 3 个 int 参数，返回三数之和
    //main 方法中分别调用两个重载方法，输出结果。
    public static void main(String[] args) {
        int result1=add(1,2);
        int resilt2=add(1,2,3);
        System.out.println(result1);
        System.out.println(resilt2);
    }
    public static int add(int a,int b){
        return a+b;
    }
    public static int add(int a,int b,int c){
        return a+b+c;
    }
}
