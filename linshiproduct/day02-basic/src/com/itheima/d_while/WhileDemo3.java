package com.itheima.d_while;

public class WhileDemo3 {
    //求1-100的偶数的个数
    static void main(String[] args) {
    print();
    }
    public static void print() {
        int count=0;
        int i=1;
        while (i<=100){
            if (i%2==0){
                count++;
            }
            i++;


        }
        System.out.println(count);

    }
}
