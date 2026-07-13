package com.atguigu.d_properties;

import java.util.Properties;
import java.util.Set;

public class Demo01 {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("qaq","qwq");
        properties.setProperty("qaq1","qwq1");
        properties.setProperty("qaq2","qwq3");
        System.out.println(properties);
        String qaq = properties.getProperty("qaq");
        System.out.println(qaq);
        Set<String> strings = properties.stringPropertyNames();
        for (String s : strings) {
            System.out.println(s+properties.getProperty(s));
        }
    }
}
