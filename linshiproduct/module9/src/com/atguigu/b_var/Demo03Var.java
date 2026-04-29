package com.atguigu.b_var;

public class Demo03Var {
    public static void main(String[] args) {
        String s=concat("-","sad","happy");
        System.out.println(s);
    }
    public static String concat(String regex,String...s){
        String str ="";
        for (int i = 0; i < s.length; i++) {
            if (i== s.length -1){
                str+=s[i];

            }else {
                str+=s[i]+regex;
            }
        }
        return str;
    }
}
