package com.atguigu.a_map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Poker {
    public static void main(String[] args) {
        String[] number = "2,3,4,5,6,7,8,9,10,J,Q,K,A".split(",");
        String[] color = "♣,♥,♠,♦".split(",");
        HashMap<Integer, String> poker = new HashMap<>();
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(1);
        int key = 2;
        for (String num : number) {
            for (String huaSe : color) {
                String pokerNumber = huaSe + num;
                poker.put(key, pokerNumber);
                list.add(key);
                key++;

            }
        }
        poker.put(0, "😊");
        poker.put(1, "☺");
       System.out.println(poker);
//        System.out.println(list);
        Collections.shuffle(list);
        ArrayList<Integer> dipai = new ArrayList<>();
        ArrayList<Integer> p1 = new ArrayList<>();
        ArrayList<Integer> p2 = new ArrayList<>();
        ArrayList<Integer> p3 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i >= 51) {
                dipai.add(list.get(i));
            } else if (i % 3 == 0) {
                p1.add(list.get(i));
            } else if (i % 3 == 1) {
                p2.add(list.get(i));
            } else if (i % 3 == 2) {
                p3.add(list.get(i));
            }
        }
//        System.out.println(dipai);
//        System.out.println(p1);
//        System.out.println(p2);
//        System.out.println(p3);
        Collections.sort(dipai);
        Collections.sort(p1);
        Collections.sort(p2);
        Collections.sort(p3);
//        System.out.println(dipai);
//        System.out.println(p1);
//        System.out.println(p2);
//        System.out.println(p3);
        method(p1,poker);
        method(p2,poker);
        method(p3,poker);
        method(dipai,poker);
    }

    private static void method(ArrayList<Integer> p, HashMap<Integer, String> poker) {
        for (Integer key : p) {
            String s = poker.get(key);
            System.out.print(s+" ");
        }
        System.out.println();
    }


}
