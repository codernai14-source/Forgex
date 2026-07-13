package com.atguigu.a_map;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Demo03HashMap {
    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        map.put("数学","科目");
        map.put("语文","科目");
        map.put("英语","科目");
        map.put("物理","科目");
        map.put("化学","科目");
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key+"---"+value);
        }

    }
}
