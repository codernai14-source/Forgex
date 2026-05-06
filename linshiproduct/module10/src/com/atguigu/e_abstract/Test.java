package com.atguigu.e_abstract;

public class Test {
    /*
    创建圆形、矩形对象
调用 showInfo() 方法输出图形信息
     */
    public static void main(String[] args) {
        Circle circle = new Circle();
        circle.color="green";
        circle.radius=3;
        circle.showInfo();
        System.out.println("===========");
        Rectangle rectangle = new Rectangle();
        rectangle.color="red";
        rectangle.height=2;
        rectangle.width=1;
        rectangle.showInfo();
    }
}
