package com.atguigu.e_innerclass;

public class Test01 {
    public static void main(String[] args) {
        Person.Heart heart =new Person.Heart();
        heart.jump();
        Person.Lung lung =new Person().new Lung();
        lung.work();
    }
}
