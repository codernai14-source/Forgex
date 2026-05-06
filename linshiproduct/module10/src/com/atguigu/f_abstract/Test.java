package com.atguigu.f_abstract;

public class Test {
    public static void main(String[] args) {
        JavaEngineer javaEngineer = new JavaEngineer(1,"a");
        javaEngineer.work();
        AndroidEngineer androidEngineer = new AndroidEngineer(2,"b");
        androidEngineer.work();
        NetEngineer netEngineer = new NetEngineer(3,"c");
        netEngineer.work();
        HardwareEngineer hardwareEngineer = new HardwareEngineer(4, "d");
        hardwareEngineer.work();
    }
}
