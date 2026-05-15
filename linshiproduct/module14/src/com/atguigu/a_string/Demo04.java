package com.atguigu.a_string;

import java.util.Objects;

public class Demo04 {
    public static void main(String[] args) {
        String s="abc";
        method(s);
        String s1="zbc";
        String s2="zbc";
        method1(s1,s2);
    }

    private static void method1(String s1,String s2) {
        if (Objects.equals(s1,s2)){
            System.out.println("yyd");
        }
    }

    private static void method(String s) {
        if ("abc".equals(s)){
            System.out.println("is abc");
        }else {
            System.out.println("no abc");
        }
    }
}
