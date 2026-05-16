package com.atguigu.a_exception;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Demo02Exception {
    public static void main(String[] args) {
        String s="a.ixt";
        try {
            add(s);
        }catch (FileNotFoundException e){
            System.out.println(e);
        }catch (IOException ioException){
            System.out.println(ioException);
        }
        delete();
        update();
        find();

    }

    private static void find() {
    }

    private static void update() {
    }

    private static void delete() {
    }

    private static void add(String s)throws IOException{
        if (!s.endsWith(".txt")){
            //造个异常
            throw new FileNotFoundException("文件没找到");
        }if (s==null){
            throw new IOException("ioexception");
        }
        System.out.println("i will get");
    }
}
