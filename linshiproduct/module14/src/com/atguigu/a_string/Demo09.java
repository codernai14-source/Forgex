package com.atguigu.a_string;

public class Demo09 {
    public static void main(String[] args) {
        String s="axc.txt";
        String[] split = s.split("\\.");
        for (int i = 0; i < split.length; i++) {
            System.out.println(split[i]);
        }
    }
}
