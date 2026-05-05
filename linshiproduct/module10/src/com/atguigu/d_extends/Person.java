package com.atguigu.d_extends;

public class Person {
    /*
    创建父类 Person
私有属性：name (姓名)、age (年龄)
提供：无参构造、有参构造、get/set 方法

     */
    private String name;
    private int age;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

