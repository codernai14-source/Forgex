package com.atguigu.f_student;

public class Test {
    public static void main(String[] args) {
        Student s1 = new Student();
        s1.setName("emma");
        s1.setSid(1);
        System.out.println(s1.getName()+s1.getSid());
        System.out.println("---------------");
        Student s2 = new Student("wuzi", 2);
        System.out.println(s2.getName()+s2.getSid());

    }
}
