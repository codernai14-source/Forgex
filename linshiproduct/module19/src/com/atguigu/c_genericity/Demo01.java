package com.atguigu.c_genericity;

import java.util.ArrayList;

public class Demo01 {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("aa");
        list.add("bb");
        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(22);
        list1.add(23);
        method(list);
        method(list1);

    }

    private static void method(ArrayList<?> list) {
        for (Object o : list) {
            System.out.println(o);
        }
    }
}
