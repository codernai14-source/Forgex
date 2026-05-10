package com.atguigu.f_innerclass;

public class Test {
    //编写测试类 Test，里面定义三个静态方法：
    //方法 1：形参为 Device 接口类型，方法内调用 work()
    //方法 2：返回值为 Electronic 抽象类类型，方法内部返回 Phone 对象
    //方法 3：形参和返回值都是 Person 普通类类型，传入 Person 对象、返回 Person 自身对象
    public static void main(String[] args) {
        //调用方法 1，传入 Phone 对象
        //调用方法 2，用抽象类类型接收返回值，并调用 showInfo()
        //调用方法 3，演示普通类传参和返回值，调用 useDevice()
        Phone phone = new Phone();
        method(phone);
        Electronic electronic =method2();
        electronic.showInfo();
        Person person=new Person();
         person=method3(person);
        person.useDevice();

    }
    public static void method(Device device){
        device.work();
    }
    public static Electronic method2(){
        return new Phone();
    }
    public static Person method3(Person person){
        return  person;
    }


}
