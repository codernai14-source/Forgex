package com.atguigu.a_math;

import java.time.LocalDate;

public class Demo10LocalDate {
    public static void main(String[] args) {
        //get();
        //with();
        //plusAndMinus();
    }

    private static void plusAndMinus() {
        LocalDate localDate = LocalDate.now();
        LocalDate localDate1 = localDate.plusYears(1l);
        System.out.println(localDate1);
        LocalDate localDate2 = localDate.minusYears(1l);
        System.out.println(localDate2);
    }

    private static void with() {
        LocalDate localDate = LocalDate.now();
        LocalDate localDate1 = localDate.withYear(2003).withMonth(12).withDayOfMonth(12);
        System.out.println(localDate1);
    }

    private static void get() {
        LocalDate localDate = LocalDate.now();
        System.out.println(localDate.getYear());
        System.out.println(localDate.getMonthValue());
        System.out.println(localDate.getDayOfMonth());
    }
}
