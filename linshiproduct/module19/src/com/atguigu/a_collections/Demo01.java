package com.atguigu.a_collections;

import java.util.ArrayList;
import java.util.Collections;

public class Demo01 {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list,"abc","efg","hij","sdg");
        System.out.println(list);
        Collections.shuffle(list);
        System.out.println(list);
        Collections.sort(list);

    }
}
