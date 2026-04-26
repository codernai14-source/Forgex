package com.atguigu.d_constructor;

public class Person {
    private String name;
    private int age;
    //无参构造
    public Person(){
        System.out.println("ws无参构造");
    }
    //有参构造

    public Person(String name,int age){
        this.name=name;
        this.age=age;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setAge(int age){
        this.age=age;
    }
    public int getAge(){
        return age;
    }
}
