package com.atguigu.c_thread;

public class Test {
    public static void main(String[] args) {
        MyThread1 t1 = new MyThread1();
        t1.setName("豆包");
        MyThread1 t2 = new MyThread1();
        t2.setName("deepseek");

        System.out.println(t1.getPriority());
        System.out.println(t2.getPriority());
        t1.setPriority(10);
        t2.setPriority(1);
        System.out.println(t1.getPriority());
        System.out.println(t2.getPriority());
        t1.start();
        t2.start();
    }
}
