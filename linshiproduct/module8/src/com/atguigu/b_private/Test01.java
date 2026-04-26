package com.atguigu.b_private;

public class Test01 {
    public static void main(String[] args) {
        Person person =new Person();
//        person.name="涛哥";
//        person.age=18;
//
//        System.out.println(person.age);
//        System.out.println(person.name);
        person.setAge(18);
        int age =person.getAge();
        System.out.println(age);
        person.setName("tg");
        String name = person.getName();
        System.out.println(name);
    }
}
