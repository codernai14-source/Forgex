package com.atguigu.a_collections;

public class Person1 implements Comparable<Person1>{
    private int age;
    private String name;

    @Override
    public String toString() {
        return "Person{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }

    public Person1(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Person1() {
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    @Override
    public int compareTo(Person1 o) {
        return this.getAge()-o.getAge();
    }
}
