package com.atguigu.e_listinlist;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Demo03 {
    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> map1 = new HashMap<>();
        map.put("1","1");
        map.put("2","1");
        map1.put("3","1");
        map1.put("4","1");
        HashMap<String, HashMap<String, String>> mapHashMap = new HashMap<>();
        mapHashMap.put("javase",map);
        mapHashMap.put("javaee",map1);

        Set<Map.Entry<String, HashMap<String, String>>> entries = mapHashMap.entrySet();
        for (Map.Entry<String, HashMap<String, String>> entry : entries) {
            HashMap<String, String> value = entry.getValue();
            Set<Map.Entry<String, String>> entries1 = value.entrySet();
            for (Map.Entry<String, String> stringStringEntry : entries1) {
                System.out.println(stringStringEntry.getKey()+stringStringEntry.getValue());
            }
        }


    }
}
