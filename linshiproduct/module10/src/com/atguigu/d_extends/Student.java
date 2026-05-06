package com.atguigu.d_extends;

public class Student extends Person{
//    私有属性：studentId (学号)
//    提供：无参构造、有参构造（使用 super 为父类私有属性赋值）
//    提供：get/set 方法
    private int studentId;

    public Student() {
    }

    public Student(String name, int age, int studentId) {
        super(name, age);
        this.studentId = studentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
}
