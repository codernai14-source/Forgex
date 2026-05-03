package com.atguigu.f_studentmanage;

public class Student {
    private int num;
    private int classroom;
    private String name;
    private String gender;

    public int getOld() {
        return old;
    }

    public void setOld(int old) {
        this.old = old;
    }

    private int old;

    public Student() {
    }

    public Student(int num, int classroom, String name, String gender) {
        this.num = num;
        this.classroom = classroom;
        this.name = name;
        this.gender = gender;
    }

    public int getClassroom() {
        return classroom;
    }

    public void setClassroom(int classroom) {
        this.classroom = classroom;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {this.gender = gender;
        }
    }

