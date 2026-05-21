package com.atguigu.a_math;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class Demo13DatetimeFormat {
    public static void main(String[] args) {
        //format();
        parse();
    }

    private static void parse() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time="2002-02-12 12:12:12";
        TemporalAccessor parse = dtf.parse(time);
        LocalDateTime from = LocalDateTime.from(parse);
        System.out.println(from);

    }

    private static void format() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        String format = dtf.format(localDateTime);
        System.out.println(format);
    }
}
