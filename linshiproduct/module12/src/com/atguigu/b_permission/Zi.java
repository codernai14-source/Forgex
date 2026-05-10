package com.atguigu.b_permission;

import com.atguigu.a_permission.Student;

public class Zi extends Student {
    public  void method() {
        System.out.println(new Student().name);
        int id1 = super.id;
    }

    public static void main(String[] args) {
        Student student=new Student();
        String name1 = student.name;
        Zi zi=new Zi();
        int id1 = zi.id;
    }

}
