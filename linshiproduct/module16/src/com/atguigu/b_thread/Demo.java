package com.atguigu.b_thread;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        MyThread myThread = new MyThread();
        myThread.start();
        myThread.setName("第五人格");


        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000L);
            System.out.println(Thread.currentThread().getName()+"线程执行了"+i);
        }

    }
}
