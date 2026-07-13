package com.atguigu.a_map;

import java.util.HashMap;
import java.util.Set;

public class Demo02HashMap {
    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        map.put("数学","科目");
        map.put("语文","科目");
        map.put("英语","科目");
        map.put("物理","科目");
        map.put("化学","科目");
        Set<String> strings = map.keySet();
        for (String string : strings) {
            System.out.println(string+map.get(string));
        }

    }
}
