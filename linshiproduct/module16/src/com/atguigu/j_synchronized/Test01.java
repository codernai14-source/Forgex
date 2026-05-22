package com.atguigu.j_synchronized;

public class Test01 {
    public static void main(String[] args) {
        Ticket ticket = new Ticket();
        Thread t1 = new Thread(ticket,"园丁");
        Thread t2 = new Thread(ticket,"医生");
        Thread t3 = new Thread(ticket,"律师");
        t1.start();
        t2.start();
        t3.start();
    }
}
