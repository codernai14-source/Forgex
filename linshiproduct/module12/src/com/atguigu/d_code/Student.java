package com.atguigu.d_code;

public class Student {
    public Student() {
        System.out.println("无参构造方法执行了");
    }
    {
        System.out.println("构造代码块执行了");
    }
    static
    {
        System.out.println("静态代码块执行了");
    }
}
