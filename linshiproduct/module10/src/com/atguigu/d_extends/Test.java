package com.atguigu.d_extends;

public class Test {
    /*
    用两种方式创建 Student 对象并赋值：
① 通过 set 方法赋值
② 通过有参构造直接赋值
打印对象的所有属性（姓名、年龄、学号）
     */

    public static void main(String[] args) {
        Student s1 = new Student();
        s1.setName("1");
        s1.setAge(18);
        s1.setStudentId(12);
        Student s2 = new Student("2", 18, 1);
        System.out.println(s1.getName()+"____"+s1.getAge()+"____"+s1.getStudentId());
        System.out.println(s2.getName()+"____"+s2.getAge()+"____"+s2.getStudentId());
    }
}
