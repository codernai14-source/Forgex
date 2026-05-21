package com.atguigu.a_math;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class Demo11PeriodandDuration {
    public static void main(String[] args) {
        //period();
        duration();
    }

    private static void duration() {
        LocalDateTime localDateTime = LocalDateTime.of(2003, 5, 5, 12, 13, 11);
        LocalDateTime localDateTime1 = LocalDateTime.of(2004, 7, 7, 18, 17, 17);
        Duration between = Duration.between(localDateTime, localDateTime1);
        between.toDays();
        System.out.println(between.toDays());
        System.out.println(between.toHours());
        System.out.println(between.toMillis());
        System.out.println(between.toMinutes());
    }

    private static void period() {
        LocalDate localDate = LocalDate.of(2003, 3, 23);
        LocalDate localDate1 = LocalDate.of(2004, 4, 24);
        Period between = Period.between(localDate, localDate1);
        System.out.println(between.getYears());
        System.out.println(between.getMonths());
        System.out.println(between.getDays());
    }
}
