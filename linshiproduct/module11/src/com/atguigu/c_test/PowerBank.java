package com.atguigu.c_test;

public class PowerBank implements PhonePeripheral{
    @Override
    public void open() {
        System.out.println("打开充电宝");
    }

    @Override
    public void close() {
        System.out.println("关闭充电宝");

    }
    public void charge(){
        System.out.println("给手机充电");
    }
}
