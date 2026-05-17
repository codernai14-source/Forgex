package com.atguigu.a_math;

import java.util.Calendar;
import java.util.Date;

public class Demo05Calendar {
    public static void main(String[] args) {
        Calendar instance = Calendar.getInstance();
        System.out.println(instance);
        int i = instance.get(Calendar.YEAR);
        System.out.println(i);
        instance.set(Calendar.YEAR,2003);
        int i1 = instance.get(Calendar.YEAR);
        System.out.println(i1);
        instance.set(2003,8,11,13,15,45);
        System.out.println(instance);
        instance.add(Calendar.YEAR,-1);
        System.out.println(instance.get(Calendar.YEAR));
        Date time = instance.getTime();
        System.out.println(time);
    }
}
