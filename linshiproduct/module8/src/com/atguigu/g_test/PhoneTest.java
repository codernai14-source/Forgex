package com.atguigu.g_test;

public class PhoneTest {
    public static void main(String[] args) {
       //在测试类中创建手机对象，分别测试合法价格、非法价格的赋值，最终输出手机信息
        Phone phone = new Phone();
        phone.setBrand("oppo");
        phone.setColor("green");
        phone.setPrice(1999);
        phone.printInfo();
        phone.setPrice(-10);
        phone.printInfo();
    }
}
