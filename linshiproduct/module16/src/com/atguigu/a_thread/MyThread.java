package com.atguigu.a_thread;

public class MyThread extends Thread{
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println(getName()+"启动!"+i);
        }
    }
}
