package com.atguigu.a_extends;

public class Zi extends Fu{
    int age=18;

    public Zi() {
        System.out.println("我是子类无参构造");
    }
    public void Zimethod(){
        System.out.println("我是子类方法");
    }
    @Override
    public void method(){
        super.method();
        System.out.println("我是子类method");
    }



}
