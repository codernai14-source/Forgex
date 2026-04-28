package com.atguigu.g_test;

public class DogTest {
    //测试类中创建对象，调用所有方法运行
    public static void main(String[] args) {
        Dog dog1 = new Dog("比格", "xiaobu", 3);
        dog1.bark();
        dog1.eat();
    }
}
