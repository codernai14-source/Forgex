package com.atguigu.l_dielock;

public class Test {
    public static void main(String[] args) {
        DieLock dieLock = new DieLock(true);
        DieLock dieLock2 = new DieLock(false);
        Thread thread = new Thread(dieLock);
        Thread thread2 = new Thread(dieLock2);
        thread.start();
        thread2.start();
    }
}
