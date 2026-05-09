package com.atguigu.c_polymorphic;

public class Dog extends Animal{
    @Override
    public void eat(){
        System.out.println("狗啃骨头");
    }
    public void watchHouse(){
        System.out.println("狗看家");
    }
}
