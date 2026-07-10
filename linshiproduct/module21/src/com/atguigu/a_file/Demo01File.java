package com.atguigu.a_file;

import java.io.File;

public class Demo01File {
    public static void main(String[] args) {
        //file01();
        file02();
    }

    private static void file02() {
        String path1="D:\\huang";
        System.out.println(path1);
        String path2="D:"+File.separator+"huang";
        System.out.println(path2);
    }

    private static void file01() {
        String pathSeparator = File.pathSeparator;
        System.out.println(pathSeparator);
        System.out.println(File.separator);
    }
}
