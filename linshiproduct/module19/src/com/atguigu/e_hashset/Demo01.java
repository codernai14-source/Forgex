package com.atguigu.e_hashset;

import java.util.HashSet;
import java.util.Iterator;

public class Demo01 {
    public static void main(String[] args) {
        HashSet<String> set = new HashSet<>();
        set.add("小一");
        set.add("小二");
        set.add("小三");
        set.add("小一");
        System.out.println(set);

        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()){
            String s = iterator.next();
            System.out.println(s);
        }
        for (String s : set) {
            System.out.println(s);
        }
    }
}
