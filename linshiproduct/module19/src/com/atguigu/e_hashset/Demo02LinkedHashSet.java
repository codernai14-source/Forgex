package com.atguigu.e_hashset;

import java.util.LinkedHashSet;

public class Demo02LinkedHashSet {
    public static void main(String[] args) {
        LinkedHashSet<String> strings = new LinkedHashSet<>();
        strings.add("1");
        strings.add("12");
        strings.add("13");
        strings.add("1");
        System.out.println(strings);

    }
}
