package com.atguigu.e_lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ticket implements Runnable{
    int ticket =100;
    Lock lock=new ReentrantLock();
    @Override
    public void run() {
        while (true) {
        try {
            Thread.sleep(100L);
            lock.lock();

            if (ticket > 0) {
                System.out.println(Thread.currentThread().getName() + "买了第" + ticket + "张票");
                ticket--;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();

        }

        }
    }
}
