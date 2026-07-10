package com.atguigu.b_genericity;

import java.util.ArrayList;

public class ListUtils {
    public static <E> void addAll(ArrayList<E> list,E...e){
        for (E e1 : e) {
            list.add(e1);
        }
    }
}
