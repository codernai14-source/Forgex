package com.atguigu.a_collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Demo01Collection {
    public static void main(String[] args) {
        Collection<String> collection=new ArrayList<>();
        collection.add("语文");
        collection.add("数学");
        collection.add("英语");
        collection.add("物理");
        collection.add("化学");
        collection.add("生物");
        Collection<String> collection1=new ArrayList<>();
        collection1.add("政治");
        collection1.add("历史");
        collection1.add("地理");
        collection.addAll(collection1);
        System.out.println(collection);
//        collection.clear();
//        System.out.println(collection);
        boolean b = collection.contains("历史");
        System.out.println(b);
        collection1.clear();
        boolean b1 = collection.isEmpty();
        boolean b2 = collection1.isEmpty();
        System.out.println(b1);
        System.out.println(b2);
        boolean b3 = collection.remove("物理");
        System.out.println(b3);
        System.out.println(collection);
        int size = collection.size();
        System.out.println(size);
        Object[] array = collection.toArray();
        System.out.println(Arrays.toString(array));
    }
}
