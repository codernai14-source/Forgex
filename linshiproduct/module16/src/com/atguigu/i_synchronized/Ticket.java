package com.atguigu.i_synchronized;

public class Ticket implements Runnable{
    int ticket =100;
    Object o=new Object();

    @Override
    public void run() {
        while (true) {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        synchronized (o) {

                if (ticket > 0) {
                    System.out.println(Thread.currentThread().getName() + "买了第" + ticket + "张票");
                    ticket--;
                }
            }
        }
    }
}
