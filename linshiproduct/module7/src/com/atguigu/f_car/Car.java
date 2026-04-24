package com.atguigu.f_car;

public class Car {
    String brand;
    String color;
    double price;
    int speed;
    //run() 方法：无返回值，控制台打印 XX品牌的汽车正在以XX km/h的速度行驶（XX 替换为当前对象的对应属性值）
    public void run(){
        System.out.println(brand+"品牌的汽车正在以"+speed+"km/h的速度行驶");
    }
    //speedUp(int addSpeed) 方法：无返回值，实现汽车提速逻辑：
    //若传入的addSpeed为正数，给当前对象的speed属性加上该值
    //若传入的addSpeed为负数，控制台打印 提速值不能为负数，提速失败
    public  void speedUp(int addSpeed){
        if (addSpeed>0){
            speed+= addSpeed;
        }else if (addSpeed<0){
            System.out.println("提速值不能为负数，提速失败");
        }

    }
    //brake() 方法：无返回值，将当前对象的speed属性置为 0，控制台打印 汽车已刹停，当前速度为0
    public void brake(){
        speed = 0;
        System.out.println("汽车已刹停，当前速度为0");
    }
}
