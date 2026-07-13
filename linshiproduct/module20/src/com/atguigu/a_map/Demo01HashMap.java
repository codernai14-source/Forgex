package com.atguigu.a_map;

import java.util.Collection;
import java.util.HashMap;

public class Demo01HashMap {
    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        map.put("数学","科目");
        map.put("语文","科目");
        map.put("英语","科目");
        map.put("物理","科目");
        map.put("化学","科目");
        System.out.println(map);
        String remove = map.remove("数学");
        System.out.println(remove);
        System.out.println(map);
        String put = map.put("物理", "理科");
        System.out.println(put);
        System.out.println(map);
        String s = map.get("化学");
        System.out.println(s);
        boolean b1 = map.containsKey("化学");
        System.out.println(b1);
        boolean b = map.containsValue("科目");
        System.out.println(b);
        Collection<String> values = map.values();
        System.out.println(values);


    }
}
