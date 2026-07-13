package com.atguigu.b_tree;

import java.util.Comparator;
import java.util.TreeSet;

public class Demo01TreeSet {
    public static void main(String[] args) {
        TreeSet<String> strings = new TreeSet<>();
        strings.add("a");
        strings.add("c");
        strings.add("b");
        strings.add("f");
        System.out.println(strings);
        TreeSet<Person> people = new TreeSet<>(new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.getAge()-o2.getAge();
            }
        });
        people.add(new Person(13,"xiaomei"));
        people.add(new Person(14,"xiaoshuai"));
        people.add(new Person(17,"xiaogou"));
        people.add(new Person(15,"xiaoming"));
        System.out.println(people);
    }
}
