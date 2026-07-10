package com.atguigu.c_hashtable_vector;

import java.util.Vector;

public class Demo02 {
    public static void main(String[] args) {
        Vector<String> vector = new Vector<>();
        vector.add("zs");
        vector.add("ls");
        for (String s : vector) {
            System.out.println(s);
        }
    }
}
