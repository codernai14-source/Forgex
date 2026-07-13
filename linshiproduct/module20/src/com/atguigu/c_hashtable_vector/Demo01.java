package com.atguigu.c_hashtable_vector;

import java.util.Hashtable;

public class Demo01 {
    public static void main(String[] args) {
        Hashtable<String, String> hashtable = new Hashtable<>();
        hashtable.put("xixi","xixi");
        hashtable.put("xixi1","xixi");
        hashtable.put("xixi","xixi");
        hashtable.put("xixi3","xixi");
        System.out.println(hashtable);
    }
}
