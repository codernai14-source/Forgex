package com.atguigu.c_test;

public class TestPhone {
    public static void main(String[] args) {
        Phone phone1 =new Phone();
        phone1.brand="oppe";
        phone1.color="绿色";
        phone1.price=2999;
        phone1.call();
        Phone phone2 =new Phone();
        phone2.brand="vivo";
        phone2.color="Green";
        phone2.price=2998;
        phone2.call();
        phone2 = phone1;
        phone1.price=5000;
        System.out.println(phone2.price);

    }
}
