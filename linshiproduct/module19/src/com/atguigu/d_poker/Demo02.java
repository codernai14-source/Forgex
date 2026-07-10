package com.atguigu.d_poker;

import java.util.ArrayList;
import java.util.Collections;

public class Demo02 {
    public static void main(String[] args) {
        ArrayList<String> color = new ArrayList<>();
//        color.add("♣");
//        color.add("♦");
//        color.add("♠");
//        color.add("♥");
        String[] split = "♣,♥,♠,♦".split(",");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            color.add(s);
        }
        ArrayList<String> num = new ArrayList<>();
        String[] split1 = "2,3,4,5,6,7,8,9,10,J,Q,K,A".split(",");
        for (int i = 0; i < split1.length; i++) {
            num.add(split1[i]);
        }
//        for (int i = 2; i <= 10; i++) {
//            num.add(i+"");
//        }
//        num.add("A");
//        num.add("J");
//        num.add("Q");
//        num.add("K");
        System.out.println(color);
        System.out.println(num);
        ArrayList<String> poker = new ArrayList<>();
        for (String s : color) {
            for (String string : num) {
                String p=s+string;
                poker.add(p);
            }
        }
        poker.add("😊");
        poker.add("☺");
        System.out.println(poker);
        Collections.shuffle(poker);
        System.out.println(poker);
        ArrayList<String> p1 = new ArrayList<>();
        ArrayList<String> p2 = new ArrayList<>();
        ArrayList<String> p3 = new ArrayList<>();
        ArrayList<String> dipai = new ArrayList<>();
        for (int i = 0; i < poker.size(); i++) {
            if (i>=51){
                dipai.add(poker.get(i));
            }else if (i%3==0){
                p1.add(poker.get(i));
            }else if (i%3==1){
                p2.add(poker.get(i));
            }else if (i%3==2){
                p3.add(poker.get(i));
            }
        }
        System.out.println(dipai);
        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);


    }
}
