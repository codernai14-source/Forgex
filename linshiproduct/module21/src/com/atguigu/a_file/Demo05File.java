package com.atguigu.a_file;

import java.io.File;
import java.io.IOException;

public class Demo05File {
    public static void main(String[] args) throws IOException {
        File file = new File("1.txt");
        File absoluteFile = file.getAbsoluteFile();
        System.out.println(absoluteFile);
        System.out.println(file.getParentFile());
        boolean newFile = file.createNewFile();
        //System.out.println(newFile);
    }
}
