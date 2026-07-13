package com.atguigu.d_copy;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Demo01CopyFile {
    public static void main(String[] args) throws Exception {
        FileInputStream fileInputStream = new FileInputStream("D:\\huang\\123.png");
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\huang\\456.png");
        byte[] bytes = new byte[1024];
        int len;
        while ((len=fileInputStream.read(bytes))!=-1){
            fileOutputStream.write(bytes,0,len);
        }
        fileInputStream.close();
        fileOutputStream.close();
    }
}
