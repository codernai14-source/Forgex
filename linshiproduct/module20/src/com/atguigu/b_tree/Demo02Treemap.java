package com.atguigu.b_tree;

import java.util.Comparator;
import java.util.TreeMap;

public class Demo02Treemap {
    public static void main(String[] args) {
        TreeMap<String, String> map = new TreeMap<>();
        map.put("a","b");
        map.put("b","b");
        map.put("d","b");
        map.put("c","b");
        System.out.println(map);

        TreeMap<Person, String> map1 = new TreeMap<>(new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.getAge()-o2.getAge();
            }
        });
        map1.put(new Person(14,"s"),"qwq");
        map1.put(new Person(16,"s"),"qwq");
        map1.put(new Person(15,"s"),"qwq");
        System.out.println(map1);
    }
}
