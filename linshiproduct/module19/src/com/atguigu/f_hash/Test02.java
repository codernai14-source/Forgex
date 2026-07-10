package com.atguigu.f_hash;

import java.util.HashSet;

public class Test02 {
    public static void main(String[] args) {
        HashSet<Person> set = new HashSet<>();
        set.add(new Person("ab",12));
        set.add(new Person("abc",123));
        set.add(new Person("ab",12));
        System.out.println(set);
    }
}
