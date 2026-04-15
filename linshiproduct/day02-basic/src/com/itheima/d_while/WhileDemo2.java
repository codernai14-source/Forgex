package com.itheima.d_while;

public class WhileDemo2 {
    static void main(String[] args) {
        //1-3sum
        int sum=0;
        int i=1;
        while (i<=3){
            sum+=i;
            i++;
        }
        System.out.println("1-3的和为："+sum);
    }

}
