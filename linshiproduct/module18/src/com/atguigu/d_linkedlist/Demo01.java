package com.atguigu.d_linkedlist;

import java.util.LinkedList;

public class Demo01 {
    public static void main(String[] args) {
        LinkedList<String> list = new LinkedList<>();
        LinkedList<String> list1 = new LinkedList<>();
        list.add("园丁");
        System.out.println(list);
        list.addFirst("医生");
        list.addLast("玩具商");
        System.out.println(list);
        list1.addLast("鹿头");
        list.addAll(list1);
        System.out.println(list);

    }
}
