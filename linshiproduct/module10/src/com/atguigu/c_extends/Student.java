package com.atguigu.c_extends;

public class Student extends Person {
    /*创建子类 Student（学生类），继承 Person
独有私有成员变量：studentId (学号)
提供有参构造方法：初始化姓名、年龄、学号（必须调用父类构造方法）
重写父类的 showInfo() 方法：输出姓名、年龄、学号
定义同名静态方法：public static void staticMethod()，输出：这是子类的静态方法（方法隐藏）
尝试调用父类的私有方法 showSecret()（验证私有方法特性）*/
    private int studentId;

    public Student(String name, int age, int studentId) {
        super(name, age);
        this.studentId = studentId;
    }
    @Override
    public void showInfo(){
        System.out.println(super.getName()+getAge()+studentId);
    }
    public static void staticMethod(){
        System.out.println("这是子类的静态方法（方法隐藏）");
    }

}
