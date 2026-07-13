package com.atguigu.b_Iterator;

import java.util.ArrayList;
import java.util.ListIterator;

public class Demo02 {
    public static void main(String[] args) {
        ArrayList<String> s = new ArrayList<>();
        s.add("1");
        s.add("2");
        s.add("3");
        s.add("13");
        s.add("31");
        s.add("13");
//        Iterator<String> iterator = s.iterator();
//        while (iterator.hasNext()){
//            if ("13".equals(iterator.next())){
//                s.add("133");
//            }
//        }
        ListIterator<String> s1 = s.listIterator();
        while (s1.hasNext()){
            String e = s1.next();
            if ("13".equals(e)){
                s1.add("133");
            }
        }
        System.out.println(s);
    }
}
