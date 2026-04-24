package com.atguigu.c_test;

public class Phone {
//    请定义一个手机类（Phone），要求如下：
//    类中包含成员变量：品牌（brand）、价格（price）、颜色（color）
//    定义打电话的方法 call ()，方法内输出正在使用XX品牌的手机打电话（XX 为当前对象的品牌值）
//    在测试类的 main 方法中，完成两个操作：
//    创建该类的 2 个独立对象，分别给成员变量赋值，并调用 call () 方法
//    创建两个引用指向同一个 Phone 对象，修改其中一个引用的 price 值，通过另一个引用输出 price 的值，验证修改效果
    String brand;
    int price;
    String color;
    public void call(){
        System.out.println("正在使用"+brand+"品牌的手机打电话");
    }
}
