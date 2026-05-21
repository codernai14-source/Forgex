package com.atguigu.e_thread;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        MyThread1 t1 = new MyThread1();
        t1.setName("豆包");
        t1.join();



        t1.start();
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName()+"执行了"+i);
        }
    }
}
