package com.atguigu.c_integer;

public class Demo04Parse {
    public static void main(String[] args) {
        //method();
        method2();
    }

    private static void method2() {
        int i = Integer.parseInt("10");

    }

    private static void method() {
        int i=10;
        String s1=i+"";
        System.out.println(s1+1);

        String s = String.valueOf(10);
        System.out.println(s+1);
    }
}
