package com.atguigu.e_abstract;

public class Circle extends Shape {
    /*
    新增成员变量 radius（半径），重写 getArea() 计算圆形面积（公式：π * radius * radius，π 取 3.14）
     */
    int radius;
    public double getArea(){
        double area = 3.14*radius*radius;
        return area;
    }
}
