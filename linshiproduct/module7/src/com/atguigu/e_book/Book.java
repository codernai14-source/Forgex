package com.atguigu.e_book;

public class Book {
    String bookName;
    String author;
    double price;
    int stock;
    public void showInfo(){
        System.out.println("《"+bookName+"》"+"作者:"+author+"价格:"+price+"元"+"库存数量:"+stock+"本");
    }
    public void sale(int num){
        if (stock>=num){
            stock-=num;
            System.out.println("卖出成功，当前剩余库存"+stock+"本");
        }else{
            System.out.println("库存不足，无法卖出");
        }
    }
}
