package com.atguigu.a_string;

import java.io.UnsupportedEncodingException;

public class Demo07 {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String s="abdecdefg";
        char[] chars=s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i]);
        }
        byte[] bytes=s.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            System.out.println(bytes[i]);
        }
        System.out.println(s.replace('a','c'));
        byte[] bytes1="你好".getBytes("utf-8");
        for (int i = 0; i < bytes1.length; i++) {
            System.out.println(bytes1[i]);
        }


    }
}
