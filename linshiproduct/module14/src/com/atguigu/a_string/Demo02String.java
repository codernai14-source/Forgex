package com.atguigu.a_string;

public class Demo02String {
    public static void main(String[] args) {
        String s1="abc";
        String s2="abc";
        String s3="ABC";
        boolean r1=s1.equals(s2);
        boolean r2=s1.equals(s3);
        boolean r3=s1.equalsIgnoreCase(s3);
        System.out.println(r1);
        System.out.println(r2);
        System.out.println(r3);
    }
}
