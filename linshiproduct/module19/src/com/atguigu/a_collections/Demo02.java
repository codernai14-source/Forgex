package com.atguigu.a_collections;

import java.util.ArrayList;
import java.util.Collections;

public class Demo02 {
    public static void main(String[] args) {
//        Person ab = new Person("ab", 18);
//        Person cd = new Person("cd", 38);
//        Person ef = new Person("ef", 28);
//        ArrayList<Person> people = new ArrayList<>();
//        Collections.addAll(people,ab,cd,ef);
        Person1 ab = new Person1("ab", 18);
        Person1 cd = new Person1("cd", 38);
        Person1 ef = new Person1("ef", 28);
        ArrayList<Person1> people1 = new ArrayList<>();
        Collections.addAll(people1,ab,cd,ef);
//        Collections.sort(people, new Comparator<Person>() {
//            @Override
//            public int compare(Person o1, Person o2) {
//                return o1.getAge()-o2.getAge();
//            }
//        });
//        System.out.println(people);
        Collections.sort(people1);
        System.out.println(people1);
   }
}
