package com.doubao.t3;

public class Test {
    public static void main(String[] args) {
        MyRunnable myRunnable = new MyRunnable();
        Thread t1 = new Thread(myRunnable,"Runnable");
        MyThread t2=new MyThread();
        t2.setName("Thread");
        t1.start();
        t2.start();
    }
}
