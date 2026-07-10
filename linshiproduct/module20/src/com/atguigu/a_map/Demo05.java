package com.atguigu.a_map;

import java.util.HashMap;
import java.util.Scanner;

public class Demo05 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.next();
        HashMap<String, Integer> map = new HashMap<>();
        char[] charArray = s.toCharArray();
        for (char c : charArray) {
            String s1 = c + "";
            if (!map.containsKey(s1)){
                map.put(s1,1);
            }else {
                Integer i = map.get(s1);
                i++;
                map.put(s1,i);
            }
        }
        System.out.println(map);
    }
}
