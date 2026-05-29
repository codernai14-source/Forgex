package com.doubao.t5;

public class Ticket implements Runnable{
    int count=0;
    int ticket=10;
    Object object=new Object();
    @Override
    public void run() {
        while (true){

                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (ticket<=0) {
                        break;
                    }
                    method();
            }
        }
        public synchronized void method(){

            count++;
            ticket--;
            System.out.println(Thread.currentThread().getName() + " 售出第" + count + "张票，剩余票数" + ticket);
        }
    }
    /*题目 4（考察：多线程共享资源 + 同步代码块 synchronized）
    经典售票案例（线程安全问题）
    需求：
    模拟车站售票：总票数 10 张，多个窗口同时售票。
    定义售票任务类，实现 Runnable 接口，共享同一个票数变量。
    开启 3 个线程 代表 3 个售票窗口，执行售票逻辑。
    要求：使用 同步代码块 解决线程安全问题，保证票数不会出现负数、重复售票。
    打印格式示例：窗口1 售出第1张票，剩余票数：9*/


