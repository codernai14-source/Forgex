package com.atguigu.c_arraylist;

import java.util.ArrayList;
import java.util.Iterator;

public class Demo01 {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("q");
        list.add("w");
        list.add("e");
        list.add("r");
        list.add("t");
        System.out.println(list);
        list.add(2,"x");
        boolean q = list.remove("q");
        String remove = list.remove(1);
        System.out.println(remove);
        System.out.println(list);
        list.set(2,"p");
        System.out.println(list);
        String s = list.get(3);
        System.out.println(s);
        int size = list.size();
        System.out.println(size);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(4);

        list1.remove(Integer.valueOf(4));
        System.out.println(list1);
    }
}
