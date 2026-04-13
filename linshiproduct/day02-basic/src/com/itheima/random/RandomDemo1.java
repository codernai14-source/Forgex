package com.itheima.random;

import java.util.Random;

public class RandomDemo1 {
    static void main(String[] args) {
        Random rd = new Random();
        int date = rd.nextInt();
        System.out.println(date);
    }
}
