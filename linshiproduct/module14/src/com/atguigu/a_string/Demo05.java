package com.atguigu.a_string;

public class Demo05 {

    public static void main(String[] args) {
        String s1="abcdefg";
        String s2="我是hqj";
        System.out.println(s1.length());
        System.out.println(s2.length());
        System.out.println(s1.concat("hhh"));
        System.out.println(s1.charAt(0));
        System.out.println(s2.charAt(0));
        System.out.println(s1.indexOf("b"));
        System.out.println(s2.indexOf("是"));
        System.out.println(s1.substring(1,2));
    }
}
