package com.itheima.operator;

public class OperatorDemo7 {
    static void main(String[] args) {
        //有两个老人的年龄分别为70，80，求两个老人的最高年龄
       int personAge1=70;
       int personAge2=80;
       int max=personAge1>personAge2?70:80;
        System.out.println("最高年龄为："+max);
        //有3个老人的年龄分别为70，80，90,求两个老人的最高年龄
        int personAge3=90;
        int max2=personAge1>personAge2?personAge1>personAge3?personAge1:personAge3:personAge2>personAge3?personAge2:personAge3;
        System.out.println(max2);
    }

}
