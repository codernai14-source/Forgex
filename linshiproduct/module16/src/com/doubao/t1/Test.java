package com.doubao.t1;

public class Test {
      /*题目 1（考察：继承 Thread 类创建线程 + sleep () + 线程命名）
需求：
自定义类 MyThread 继承 Thread 类。
在 run() 方法中实现功能：循环打印 1~5，每打印一个数字，让线程休眠 500 毫秒。
在测试类 Test01 中：
创建 2 个 MyThread 线程对象；
分别给两个线程设置名称：线程A、线程B；
调用方法开启线程，观察运行效果。*/
      public static void main(String[] args) {
          MyThread a = new MyThread();
          MyThread b = new MyThread();
          a.setName("线程A");
          b.setName("线程B");
          a.start();
          b.start();
      }
}
