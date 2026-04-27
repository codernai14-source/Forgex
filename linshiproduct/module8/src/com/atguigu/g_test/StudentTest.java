package com.atguigu.g_test;

import java.util.Scanner;

public class StudentTest {
//    在主方法中创建 2 个学生对象，分别给属性赋值，调用showInfo()方法输出信息
public static void main(String[] args) {
    Student s1 = new Student();
    s1.setAge(18);
    s1.setName("July");
    s1.setScore(90);
    Student s2 = new Student();
    s2.setAge(18);
    s2.setName("Bob");
    s2.setScore(89);
    s1.showInfo();
    s2.showInfo();
    //在主方法中：
    //使用无参构造创建对象，再通过 setter 设置属性
    //使用全参构造直接创建带完整数据的对象
    //两个对象都调用 showInfo () 输出
    Student s3 = new Student();
    s3.setAge(18);
    s3.setName("emma");
    s3.setScore(70);
    Student s4 = new Student("lily", 78, 18);
    s3.showInfo();
    s4.showInfo();
    //在 main 方法中，创建一个长度为 3 的Student 类型对象数组
    //键盘录入 3 个学生的姓名、年龄、成绩，封装为 3 个学生对象存入数组
    Student[] student = new Student[3];
    Scanner sc = new Scanner(System.in);
    for (int i = 0; i < student.length; i++) {
    String name = sc.next();
    int age = sc.nextInt();
    double score = sc.nextDouble();
        Student stu = new Student();
        stu.setAge(age);
        stu.setName(name);
        stu.setScore(score);
        student[i]=stu;
    }
    for (int i = 0; i < student.length; i++) {
        student[i].showInfo();
    }
}
}
