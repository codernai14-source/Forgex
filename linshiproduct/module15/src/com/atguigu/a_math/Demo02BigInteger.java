package com.atguigu.a_math;

import java.math.BigInteger;

public class Demo02BigInteger {
    public static void main(String[] args) {
        BigInteger b1 = new BigInteger("111111111111111111111111111111111111111111");
        BigInteger b2 = new BigInteger("111111111111111111111111111111111111111111");
        System.out.println(b1.add(b2));
        System.out.println(b1.subtract(b2));
        System.out.println(b1.multiply(b2));
        System.out.println(b1.divide(b2));
        int i = b1.intValue();
        System.out.println(i);
    }
}
