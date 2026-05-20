package com.atguigu.a_math;

import java.time.LocalDate;

public class Demo08LocalDate {
    public static void main(String[] args) {
        LocalDate now = LocalDate.now();
        System.out.println(now);
        LocalDate localDate = LocalDate.of(2003, 8, 11);
        System.out.println(localDate);
    }
}
