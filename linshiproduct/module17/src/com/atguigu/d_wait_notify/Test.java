package com.atguigu.d_wait_notify;

public class Test {
    public static void main(String[] args) {
        BaoZiPu baoZiPu = new BaoZiPu();
        Consumer consumer = new Consumer(baoZiPu);
        Product product = new Product(baoZiPu);
        new Thread(consumer).start();
        new Thread(consumer).start();
        new Thread(consumer).start();
        new Thread(product).start();
        new Thread(product).start();
        new Thread(product).start();
    }
}
