package com.atguigu.b_output;

import java.io.FileOutputStream;
import java.io.IOException;

public class Demo01 {
    public static void main(String[] args) throws IOException {
        //method();
        //method02();
        //methdo03();
        method04();
    }

    private static void method04() throws IOException {
        FileOutputStream fos = new FileOutputStream("linshiproduct\\module21\\1.txt",true);
        fos.write("床前明月光\r\n".getBytes());
        fos.write("疑是地上霜\n".getBytes());
        fos.write("举头望明月\n".getBytes());
        fos.write("低头思故乡\n".getBytes());
        fos.close();
    }

    private static void methdo03() throws IOException {
        FileOutputStream fos = new FileOutputStream("linshiproduct\\module21\\1.txt");
        byte[] b={97,98,99,100,101};
        fos.write(b,2,2);
        fos.close();
    }

    private static void method02() throws IOException {
        FileOutputStream fos = new FileOutputStream("linshiproduct\\module21\\1.txt");
        byte[] b={97,98,99};
        fos.write(b);
        fos.close();
    }

    private static void method() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("linshiproduct\\module21\\1.txt");
        //fileOutputStream.write(97);
        fileOutputStream.close();
    }
}
