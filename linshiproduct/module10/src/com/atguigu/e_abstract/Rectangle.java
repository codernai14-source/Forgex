package com.atguigu.e_abstract;

public class Rectangle extends Shape {
    /*
    Rectangle（矩形）：新增成员变量 width（宽）、height（高），
    重写 getArea() 计算矩形面积（公式：width * height）
     */
    int width;
    int height;
    @Override
    public double getArea() {
        double area = width*height;
        System.out.println(area);
        return area;
    }
}
