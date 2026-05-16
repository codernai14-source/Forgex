package com.atguigu.b_object;

public class Test {
    public static void main(String[] args) throws CloneNotSupportedException {
        Person p1=new Person(18,"July");
        System.out.println(p1);
        System.out.println(p1.toString());
        Person p2=new Person(19,"Emma");
        Person p3=new Person(18,"July");
        boolean result=p1.equals(p2);
        System.out.println(result);
        boolean result2=p1.equals(p3);
        System.out.println(result2);
        Object o = p3.clone();
        Person p4=(Person)o;
        System.out.println("=============");
        System.out.println(p4);
    }
}
