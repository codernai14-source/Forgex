package com.atguigu.a_math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Demo03BigDecimal {
    public static void main(String[] args) {
        BigDecimal b1 = new BigDecimal("3.44");
        BigDecimal b2 = BigDecimal.valueOf(3.44);
        System.out.println(b1.add(b2));
        System.out.println(b1.subtract(b2));
        System.out.println(b1.multiply(b2));
        System.out.println(b1.divide(b2));
        BigDecimal b3 = new BigDecimal("1");
        BigDecimal b4 = BigDecimal.valueOf(3);
        //System.out.println(b3.divide(b4));
        System.out.println(b3.divide(b4,3,BigDecimal.ROUND_HALF_UP));
        BigDecimal d1 = b3.divide(b4, 3, BigDecimal.ROUND_HALF_UP);
        double v = d1.doubleValue();
        System.out.println(v);
        BigDecimal d2 = b3.divide(b4, 2, RoundingMode.HALF_UP);
        System.out.println(d2);
    }
}
