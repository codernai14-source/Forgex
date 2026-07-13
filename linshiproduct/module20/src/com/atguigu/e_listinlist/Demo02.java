package com.atguigu.e_listinlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Demo02 {
    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> map1 = new HashMap<>();
        map.put("西瓜","红色");
        map.put("西红柿","绿色");
        map1.put("苹果","红色");
        map1.put("梨子","黄色");
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        list.add(map);
        list.add(map1);
        for (HashMap<String, String> hashMap : list) {
            Set<String> strings = hashMap.keySet();
            for (String string : strings) {
                System.out.println(string+hashMap.get(string));
            }
        }
    }
}
