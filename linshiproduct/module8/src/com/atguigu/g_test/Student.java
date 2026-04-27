package com.atguigu.g_test;

public class Student {
//    编程题 1：类与对象基础
//    定义一个学生 Student类：
//    私有成员属性：姓名name、年龄age、成绩score
//            为所有属性提供setter和getter方法

    private String name;
    private  int age;
    private double score;

//    给 Student 类添加无参构造方法
//    给 Student 类添加全参构造方法（一次性给姓名、年龄、成绩赋值）


    public Student() {
    }

    public Student(String name, double score, int age) {
        this.name = name;
        this.score = score;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;

    }
//    定义一个成员方法showInfo()，用来打印学生的全部信息
    public void showInfo(){
        System.out.println("姓名"+name+"年龄"+age+"成绩"+score);
    }
}
