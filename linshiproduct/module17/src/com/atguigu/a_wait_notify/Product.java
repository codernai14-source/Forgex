package com.atguigu.a_wait_notify;

public class Product implements Runnable{
    private BaoZiPu baoZiPu;

    public Product(BaoZiPu baoZiPu) {
        this.baoZiPu = baoZiPu;
    }

    @Override
    public void run(){
        while (true){
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
           synchronized (baoZiPu){
               if (baoZiPu.isFlag()==true){
                   try {
                       baoZiPu.wait();
                   } catch (InterruptedException e) {
                       throw new RuntimeException(e);
                   }
               }
               baoZiPu.setCount();
               baoZiPu.setFlag(true);
               baoZiPu.notify();
           }
        }
    }

}
