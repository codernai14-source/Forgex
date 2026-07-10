package com.atguigu.f_hash;

public class Demo01Hash {
    public static void main(String[] args) {
        Person e1 = new Person("emma", 15);
        Person e2 = new Person("emma", 15);
        System.out.println(e1);
        System.out.println(e2);
        System.out.println(e1.hashCode());
        System.out.println(e2.hashCode());
//        System.out.println(Integer.toHexString(295530567));
//        System.out.println(Integer.toHexString(2003749087));
        System.out.println("++++++++++++++++++++");
        String s1="abc";
        String s2=new String("abc");
        System.out.println(s1.hashCode());
        System.out.println(s2.hashCode());

        System.out.println("++++++++++++++++++++");
        String s3="通话";
        String s4="重地";
        System.out.println(s3.hashCode());
        System.out.println(s4.hashCode());

    }
}
