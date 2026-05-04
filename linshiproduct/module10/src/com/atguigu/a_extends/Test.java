package com.atguigu.a_extends;

public class Test {
    public static void main(String[] args) {
        Zi zi1 = new Zi();
        System.out.println(zi1.age);
        System.out.println(zi1.name);
        zi1.Fumethod();
        zi1.Zimethod();
        zi1.method();
        System.out.println("---------------------------");

        Fu fu1 = new Fu();
        System.out.println(fu1.age);
        fu1.Fumethod();
        fu1.method();


    }
}
