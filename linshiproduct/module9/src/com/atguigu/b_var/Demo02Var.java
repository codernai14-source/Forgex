package com.atguigu.b_var;

public class Demo02Var {
    public static void main(String[] args) {
        String s=concat();
        System.out.println(s);
    }
    public static String concat(String...arr){
        String str ="";
        for (int i = 0; i < arr.length; i++) {
            str+=arr[i];
        }
        return str;
    }
}
