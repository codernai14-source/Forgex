package com.atguigu.g_test;

public class Phone {
//    私有属性：品牌brand、价格price、颜色color
//            提供打印手机全部属性的成员方法
     private String brand;
     private double price;
     private String color;

    public double getPrice() {
        return price;
    }
//    要求：价格范围必须在0~20000之间，设置非法价格时，默认赋值为999

    public void setPrice(double price) {
        if (price<20000&&price>0){
            this.price = price;
        }else {
            this.price=999;
        }
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    public void printInfo(){
        System.out.println("品牌"+brand+"颜色"+color+"价格"+price);

    }
}
