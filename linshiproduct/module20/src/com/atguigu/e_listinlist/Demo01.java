package com.atguigu.e_listinlist;

import java.util.ArrayList;

public class Demo01 {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();
        list.add("西瓜");
        list.add("苹果");
        list2.add("西红柿");
        list2.add("黄瓜");
        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        arrayLists.add(list);
        arrayLists.add(list2);
        for (ArrayList<String> arrayList : arrayLists) {
            for (String s : arrayList) {
                System.out.println(s);
            }
        }

    }
}
