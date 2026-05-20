package com.atguigu.a_math;

import java.time.LocalDateTime;

public class Demo09LocalDateTime {
    public static void main(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println(localDateTime);
        LocalDateTime localDateTime1 = LocalDateTime.of(2003, 8, 11, 21, 11, 11);
        System.out.println(localDateTime1);
    }
}
