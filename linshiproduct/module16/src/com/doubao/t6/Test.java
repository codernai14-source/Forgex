package com.doubao.t6;

public class Test {
    public static void main(String[] args) {
        LockA lockA=new LockA();
        LockB lockB=new LockB();
        DieLock dieLock = new DieLock(lockA,lockB,true);
        DieLock dieLock2 = new DieLock(lockA,lockB,false);
        Thread thread = new Thread(dieLock);
        Thread thread2 = new Thread(dieLock2);
        thread.start();
        thread2.start();

    }
}
