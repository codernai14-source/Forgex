package com.atguigu.d_thread;

public class Test {
    public static void main(String[] args) {
        MyThread1 t1 = new MyThread1();
        t1.setName("豆包");
        MyThread2 t2 = new MyThread2();
        t2.setName("deepseek");
        t2.setDaemon(true);
        t1.start();
        t2.start();
    }
}
