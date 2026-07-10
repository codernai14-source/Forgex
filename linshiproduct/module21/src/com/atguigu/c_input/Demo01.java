package com.atguigu.c_input;

import java.io.FileInputStream;
import java.io.IOException;

public class Demo01 {
    public static void main(String[] args) throws Exception{
        //method01();
        method02();
    }

    private static void method02()throws Exception {
        FileInputStream fis = new FileInputStream("linshiproduct\\module21\\1.txt");
        byte[] bytes=new byte[1024];
        int read;
        while ((read= fis.read(bytes))!=-1){
            System.out.println(read);
            System.out.println(new String(bytes,0,read));

        }
    }

    private static void method01() throws IOException {
        FileInputStream fis = new FileInputStream("linshiproduct\\module21\\1.txt");
//        int read = fis.read();
//        System.out.println(read);
//        System.out.println(fis.read());
//        System.out.println(fis.read());
//        System.out.println(fis.read());
//        System.out.println(fis.read());
//        System.out.println(fis.read());
//        System.out.println(fis.read());
        int len;
        while ((len=fis.read())!=-1){
            System.out.println((char)len);
        }
        fis.close();
    }
}
