package com.atguigu.a_wait_notify;

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

    public void getCount() {
        System.out.println("第。。。。。。"+count+"个包子被卖出");
    }

    public void setCount() {
        count++;
        System.out.println("第——————————"+count+"个包子被制作出来了");
    }
}
