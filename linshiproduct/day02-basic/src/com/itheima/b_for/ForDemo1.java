package com.itheima.b_for;

public class ForDemo1 {
    //求1-100的偶数和
    static void main(String[] args) {
        int i = 1;
        int sum = 0;
        for (i = 1; i <= 100; i++) {
            if (i % 2 == 0) {
                sum += i;
            }
        }
        System.out.println(sum);

    }
}
