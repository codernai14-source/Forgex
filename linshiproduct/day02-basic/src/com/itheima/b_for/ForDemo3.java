package com.itheima.b_for;

public class ForDemo3 {
    //求1-100的偶数的个数
    static void main(String[] args) {
    print();
    }
    public static void print() {
        int count=0;
        int i=1;
        for (i=1;i<=100;i++){
            if (i%2==0){
                count++;
            }
        }
        System.out.println(count);

    }


    }

