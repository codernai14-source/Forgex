package com.atguigu.a_exception;

public class Demo03Finally {
//    编写 Java 程序，实现 try-catch-finally 结构；
//    在 try 块中写除法运算，模拟算术异常（除数为 0）；
public static void main(String[] args) {
    int result=method(2,0);
    System.out.println(result);


}

    public static int method(int a ,int b) {

        try {
            return a/b;
        } catch (Exception e) {
            System.out.println(e);
           return -1;
        }finally {
            System.out.println("资源已手动释放关闭");
        }


    }
//            catch 块捕获算术异常，打印异常提示信息；
//            finally 块中固定输出一句：「资源已手动释放关闭」；
//    分两种情况运行代码：
//    情况 1：正常除法（如 10 / 2，无异常）
//    情况 2：除数为 0（如 10 / 0，触发异常）
//    额外拓展测试：
//    在 catch 里加一行 return;，观察 finally 还会不会执行；
//    再单独测试：在 try 里加 System.exit(0)，观察 finally 是否执行。
}
