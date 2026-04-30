package com.atguigu.g_test;


public class StudentOperator {
    private Student s1;

    public StudentOperator(Student s1) {
        this.s1 = s1;
    }

    public void showInfo(){
        System.out.println("姓名"+s1.getName()+"年龄"+s1.getAge()+"成绩"+ s1.getScore());
    }
}
