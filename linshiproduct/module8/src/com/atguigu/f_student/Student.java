package com.atguigu.f_student;

public class Student {
    private int sid;
    private String name;

    public Student() {
    }

    public Student(String name, int sid) {
        this.name = name;
        this.sid = sid;
    }

    public int getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public void setName(String name) {
        this.name = name;
    }
}
