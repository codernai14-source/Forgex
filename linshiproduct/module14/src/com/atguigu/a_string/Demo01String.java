package com.atguigu.a_string;

public class Demo01String {
    public static void main(String[] args) {
        String s1 = new String();
        System.out.println(s1);
        String s2 = new String("abc");
        System.out.println(s2);
        char[] value={'a','b','c'};
        String s3 = new String(value);
        System.out.println(s3);
        byte[] bytes={97,98,99};
        String s4 = new String(bytes);
        System.out.println(s4);
        String s5 = new String(value, 1, 2);
        System.out.println(s5);
        String s6 = new String(bytes, 2, 1);
        System.out.println(s6);

    }
}
