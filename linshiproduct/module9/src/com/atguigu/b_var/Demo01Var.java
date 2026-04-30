package com.atguigu.b_var;

public class Demo01Var {
    public static void main(String[] args) {
        sum(1,45,54,4);
        sum1(1,34,5,5);
    }
    public static void sum(int...arr){
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum+=arr[i];
        }
        System.out.println(sum);
    }
    public static void sum1(int i,int...arr){

    }
}
