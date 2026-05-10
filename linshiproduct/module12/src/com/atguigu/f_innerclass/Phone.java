package com.atguigu.f_innerclass;

public class Phone extends Electronic implements Device{
    @Override
    public void work() {
        System.out.println("手机正在工作，刷视频");
    }

    @Override
    public void showInfo() {
        System.out.println("这是一台智能手机");

    }
    //定义手机类 Phone：
    //继承 Electronic 抽象类
    //实现 Device 接口
    //重写 work() 方法：打印「手机正在工作，刷视频」
    //重写 showInfo() 方法：打印「这是一台智能手机」

}
