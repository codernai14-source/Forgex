package com.atguigu.a_exception;

public class Demo01Exception {
    //定义一个方法 checkSuffix(String fileName)
    //功能要求：判断传入的文件名是否以 .txt 结尾
    //如果不是以 .txt 结尾：
    //手动创建异常对象
    //使用 throw 手动抛出这个异常
    //如果是以 .txt 结尾：控制台打印：文件格式合法
    //在 main 方法中调用该方法，分别测试两组数据：
    //测试 1："笔记.txt"
    //测试 2："视频.mp4"
    public static void main(String[] args) {
        checkSuffix("视频.mp4");
    }
    public static void checkSuffix(String fileName){
        if (!fileName.endsWith(".txt")){
            throw new NullPointerException();
        }
        System.out.println("文件格式合法");
    }
}
