package com.itheima.random;

import java.util.Random;

public class RandomDemo2 {
    static void main(String[] args) {
        Random rd = new Random();
        int date = rd.nextInt(10)+1;
        System.out.println(date);
    }
}
