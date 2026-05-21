package com.atguigu.a_thread;

public class Demo {
    public static void main(String[] args) {
        MyThread myThread = new MyThread();
        myThread.start();
        myThread.setName("第五人格");


        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName()+"线程执行了"+i);
        }

    }
}
