package com.atguigu.b_private;

public class Person {
    private String name;
    private int age;
    //为name赋值
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setAge(int age){
        if (age>150||age<0){
            System.out.println("年龄不合理");
        }else {
            this.age = age;
        }
    }
    public int getAge(){
        return age;
    }
}
