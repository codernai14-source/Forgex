package com.atguigu.a_math;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Demo07SimpleDateFormat {
    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(new Date());
        System.out.println(dateStr);
        Date date=sdf.parse(dateStr);
        System.out.println(date);
        String time="2003-03-13 12:02:21";
        Date date1 = sdf.parse(time);
        System.out.println(date1);
    }
}
