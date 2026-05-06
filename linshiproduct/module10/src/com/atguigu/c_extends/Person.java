package com.atguigu.c_extends;

public class Person {
    /*创建父类 Person（人类）
私有成员变量：name (姓名)、age (年龄)
提供有参构造方法：初始化姓名和年龄
私有方法：private void showSecret()，输出：这是父类的私有方法，无法被重写
普通成员方法：public void showInfo()，输出姓名和年龄
静态方法：public static void staticMethod()，输出：这是父类的静态方法*/
    private String name;
    private int age;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    private void showSecret(){
        System.out.println("这是父类的私有方法，无法被重写");
    }
    public void showInfo(){
        System.out.println(this.name+this.age);
    }
    public static void staticMethod(){
        System.out.println("这是父类的静态方法");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
