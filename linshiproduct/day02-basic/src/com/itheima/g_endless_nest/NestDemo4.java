package com.itheima.g_endless_nest;

public class NestDemo4 {
    static void main(String[] args) {
        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                System.out.print("*");

            }
            System.out.println();
        }
    }
}
