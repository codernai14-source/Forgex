package com.atguigu.c_test;

public class Test {
    public static void main(String[] args) {

        Phone phone=new Phone();
        phone.start();
        HeadSet headSet=new HeadSet();
        phone.usePeripheral(headSet);

        System.out.println("=========================");
        PowerBank powerBank=new PowerBank();
        phone.usePeripheral(powerBank);
        phone.shutdown();
    }
}
