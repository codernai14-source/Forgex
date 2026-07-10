package com.atguigu.b_genericity;

import java.util.ArrayList;

public class Demo03 {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        ListUtils.addAll(list,"we");
        System.out.println(list);
    }
}
