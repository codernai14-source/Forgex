package com.atguigu.b_array;

import java.util.Scanner;

public class Demo06Search {
    static void main(String[] args) {
        int[] arr = {0, 1, 2, 3, 4};
        Scanner sc = new Scanner(System.in);
        int date = sc.nextInt();
        int flag = 0;
        for (int i = 0; i < arr.length; i++) {
            if (date == arr[i]) {
                System.out.println("[" + i + "]");
                flag++;
            }
        }
        if (flag == 0) {
            System.out.println("没找到");
        }
    }
}
