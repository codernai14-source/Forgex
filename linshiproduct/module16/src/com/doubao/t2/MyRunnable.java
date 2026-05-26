package com.doubao.t2;

public class MyRunnable implements Runnable{
    @Override
    public void run() {
        for (int i = 0; i < 4; i++) {
            System.out.println(Thread.currentThread().getName()+"正在执行任务");
        }
    }
    /*题目 2（考察：实现 Runnable 接口 + 匿名内部类创建线程）
需求：
方式一：自定义类 MyRunnable 实现 Runnable 接口，run() 中循环打印 "正在执行任务"，一共打印 4 次。
方式二：使用匿名内部类 形式，再创建一个线程，循环打印 "匿名线程执行中"，一共打印 4 次。
在测试类 Test02 中，分别启动以上两个线程。*/

}
