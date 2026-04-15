package com.itheima.h_game;

import java.util.Random;
import java.util.Scanner;

public class GameDemo {
    static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        Random rd =new Random();
        int daan= rd.nextInt(100)+1;
        while (true){
            System.out.println("请输入一个1-100之间的数字");
            int num= sc.nextInt();
            if (num == daan) {
                System.out.println("恭喜你，答对啦！");
                break;
            } else if (num > daan) {
                System.out.println("大了");
            } else {
                System.out.println("小了");
            }
        }

    }


}
