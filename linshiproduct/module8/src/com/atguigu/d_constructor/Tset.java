package com.atguigu.d_constructor;

public class Tset {
    public static void main(String[] args) {
        Person person = new Person();
        Person person2 = new Person("无名", 18);

        System.out.println(person2.getName()+"11111"+person2.getAge());
    }

}
