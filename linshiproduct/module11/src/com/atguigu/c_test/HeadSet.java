package com.atguigu.c_test;

public class HeadSet implements PhonePeripheral{
    @Override
    public void open() {
        System.out.println("开启耳机");
    }

    @Override
    public void close() {
        System.out.println("关闭耳机");

    }

    public void playMusic(){
        System.out.println("播放音乐");
    }
}
