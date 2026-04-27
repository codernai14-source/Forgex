package com.atguigu.g_test;

public class Dog {
    //按照标准 JavaBean 规范，编写一个Dog标准类：
    //私有成员变量：品种、昵称、年龄
    //空参构造、全参构造
    //全部属性的 getter/setter
    private String kind;
    private String name;
    private int age;

    public Dog() {
    }

    public Dog(String kind, String name, int age) {
        this.kind = kind;
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    //额外自定义成员方法：eat()（打印 “狗狗正在吃东西”）、bark()（打印 “狗狗正在汪汪叫”）
    public void eat(){
        System.out.println("狗狗正在吃东西");
    }
    public void bark(){
        System.out.println("狗狗正在汪汪叫");
    }
}
