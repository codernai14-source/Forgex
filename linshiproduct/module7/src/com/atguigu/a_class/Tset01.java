package com.atguigu.a_class;

public class Tset01 {
    public static void main(String[] args) {
        Citizen my = new Citizen();
        my.name="无名";
        my.idCard="54123";
        my.birthday.year=2000;
        my.birthday.month=12;
        my.birthday.day=30;
        System.out.println(my.birthday.year);
    }
}
