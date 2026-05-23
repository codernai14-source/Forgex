package com.atguigu.h_ticket;

public class Ticket implements Runnable{
    int ticket =100;

    @Override
    public void run() {
        while (true) {
            if (ticket > 0) {
                System.out.println(Thread.currentThread().getName() + "买了第" + ticket + "票");
                ticket--;
            }
        }
    }
}
