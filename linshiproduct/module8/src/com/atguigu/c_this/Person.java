package com.atguigu.c_this;

public class Person {
    String name;
    //哪个对象调用了this所在的方法，this就代表哪个对象
    public void speak(String name){
        System.out.println(this+"+++++++++");
        System.out.println(this.name+"你好，我是"+name);
    }
}
