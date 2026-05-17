package com.atguigu.a_math;

import java.util.Date;

public class Demo04Date {
    public static void main(String[] args) {
        Date date = new Date();
        System.out.println(date);
        Date date1 = new Date(1000l);
        System.out.println(date1);
        long time = date1.getTime();
        System.out.println(time);
        long time1 = date.getTime();
        System.out.println(time1);
        date1.setTime(60000l);
        System.out.println(date1);
        date.setTime(60000l);
        System.out.println(date);
        System.out.println(date1.getTime());
    }
}
