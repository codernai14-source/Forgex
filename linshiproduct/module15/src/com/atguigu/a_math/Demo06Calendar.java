package com.atguigu.a_math;

import java.util.Calendar;
import java.util.Scanner;

public class Demo06Calendar {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int i = sc.nextInt();
        Calendar c = Calendar.getInstance();
        c.set(i,2,1);
        c.add(Calendar.DAY_OF_MONTH,-1);
        int i1 = c.get(Calendar.DAY_OF_MONTH);
        if (i1==29){
            System.out.println("闰年");
        }else {
            System.out.println("平年");
        }

    }
}
