package com.atguigu.e_abstract;

public abstract class Shape {
    /*
    定义抽象父类 Shape（图形）
包含成员变量：color（颜色）
提供构造方法：初始化颜色
定义抽象方法：getArea() → 作用：计算图形面积（无具体实现）
定义普通方法：showInfo() → 输出：图形的颜色 + 面积

     */
    String color;

    public Shape() {
    }

    public Shape(String color) {
        this.color = color;
    }

    public abstract double getArea();

    public void showInfo(){
        System.out.println(this.color);
        double area= getArea();
        System.out.println(area);
    }
}
