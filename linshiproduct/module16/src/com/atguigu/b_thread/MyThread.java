package com.atguigu.b_thread;

public class MyThread extends Thread{
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            //线程睡眠
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(getName()+"启动!"+i);

        }
    }
}
