package com.atguigu.b_stringbuilder;

public class Demo01 {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder("abc");
        System.out.println(sb);
        System.out.println(sb1);
        System.out.println("========================");
        sb.append("abcd").append("saf");
        System.out.println(sb);
        sb.reverse();
        System.out.println(sb);
        String s = sb.toString();


    }
}
