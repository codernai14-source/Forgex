package com.doubao.t3;

public class MyThread extends Thread{
    @Override
    public void run(){
        System.out.println(Thread.currentThread().getName());
    }
}
