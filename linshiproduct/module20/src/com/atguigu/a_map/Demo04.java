package com.atguigu.a_map;

import java.util.HashMap;

public class Demo04 {
    public static void main(String[] args) {
        HashMap<Person, String> map = new HashMap<>();
        map.put(new Person("zs",14),"liuyan");
        map.put(new Person("ls",14),"liuyan");
        map.put(new Person("zs",14),"changsha");
        System.out.println(map);
    }
}
