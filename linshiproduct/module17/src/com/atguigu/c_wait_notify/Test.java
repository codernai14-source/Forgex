package com.atguigu.c_wait_notify;

public class Test {
    public static void main(String[] args) {
        BaoZiPu baoZiPu = new BaoZiPu();
        Consumer consumer = new Consumer(baoZiPu);
        Product product = new Product(baoZiPu);
        Thread t1 = new Thread(consumer);
        Thread t2 = new Thread(product);
        t1.start();
        t2.start();
    }
}
