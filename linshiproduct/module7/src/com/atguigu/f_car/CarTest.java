package com.atguigu.f_car;

public class CarTest {
    //创建 2 个Car类的对象，分别给两个对象的所有成员变量赋值
    public static void main(String[] args) {
        Car car1=new Car();
        car1.speed=50;
        car1.price=5000;
        car1.brand="比亚迪";
        car1.color="green";
        Car car2=new Car();
        car2.speed=60;
        car2.price=6000;
        car2.brand="奔驰";
        car2.color="黑色";
        //分别调用两个对象的run()方法，输出车辆行驶信息
        //给第一个汽车对象调用speedUp()方法，传入合法正数提速，再次调用run()验证速度变化
        //给第二个汽车对象调用speedUp()方法，传入负数，验证错误提示效果
        car1.run();
        car2.run();
        car1.speedUp(30);
        car1.run();
        car2.speedUp(-40);
    }
}
