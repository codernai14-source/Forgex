package com.atguigu.b_wait_notify;

public class BaoZiPu {
    private boolean flag;
    private int count;

    public BaoZiPu(boolean flag, int count) {
        this.flag = flag;
        this.count = count;
    }

    public BaoZiPu() {
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public synchronized void getCount() {
        if (this.isFlag()==false){
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("第。。。。。。"+count+"个包子被卖出");
        this.setFlag(false);
        this.notify();
    }


    public synchronized void setCount() {
        if (this.isFlag()==true){
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        count++;
        System.out.println("第——————————"+count+"个包子被制作出来了");
        this.setFlag(true);
        this.notify();
    }
}
