package com.doubao.t6;

public class DieLock implements Runnable{
     LockA lockA=new LockA();
     LockB lockB=new LockB();
    Boolean flag;

    public DieLock(LockA lockA, LockB lockB, Boolean flag) {
        this.lockA = lockA;
        this.lockB = lockB;
        this.flag = flag;
    }

    public DieLock() {
    }

    @Override
    public void run() {
        if (flag) {

            synchronized (lockA) {
                System.out.println("a1开了");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (lockB) {
                    System.out.println("b1开了");
                }
            }
        }else {
            synchronized (lockB) {
                System.out.println("b2开了");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (lockA) {
                    System.out.println("a2开了");
                }
            }
        }
    }
}
