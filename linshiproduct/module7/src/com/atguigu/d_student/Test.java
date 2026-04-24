package com.atguigu.d_student;

public class Test {
    public static void main(String[] args) {
        Student s1 =new Student();
        s1.name="boniu";
        s1.chinese=100;
        s1.math=100;
        System.out.println(s1.name+"的总成绩是"+(s1.chinese+s1.math));
        System.out.println(s1.name+"d"+(s1.chinese+ s1.math)/2);
        Student s2 =new Student();
        s2.name="zongjie";
        s2.chinese=50;
        s2.math=100;
        s2.sum();
        s2.average();

    }
}
