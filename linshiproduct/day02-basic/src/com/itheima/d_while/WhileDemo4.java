package com.itheima.d_while;

public class WhileDemo4 {
    static void main(String[] args) {
       print();
    }
    public static void print() {
        int mountain= 8844430;
        double paper=0.1;
        int count =0;
        while (paper<mountain){
            paper=paper*2;
            count++;
        }
        System.out.println(count);

    }
}
