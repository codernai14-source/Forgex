package com.atguigu.c_extends;

public class Test {
    public static void main(String[] args) {
        /*
        创建 Student 对象
调用重写后的 showInfo() 方法
分别通过类名调用父类、子类的静态方法
验证构造方法、私有方法、静态方法的特性
         */
        Student s1 = new Student("name",18,1);
        s1.showInfo();
        s1.staticMethod();
        //s1.showSecret();
        Person.staticMethod();
        Student.staticMethod();
    }
}
