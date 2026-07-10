package com.atguigu.a_file;

import java.io.File;

public class Demo02File {
    public static void main(String[] args) {
//        File(String parent, String child) 根据所填写的路径创建File对象
//        parent:父路径
//        child:子路径
        File file = new File("D:\\huang", "list\\Demo01.txt");
        System.out.println(file);
//        File(File parent, String child)  根据所填写的路径创建File对象
//        parent:父路径,是一个File对象
//        child:子路径
        File father = new File("D:\\huang");
        File file1 = new File(father, "list\\Demo01.txt");
        System.out.println(file1);
//        File(String pathname)  根据所填写的路径创建File对象
//        pathname:直接指定路径
        File file2 = new File("D:\\huang\\list\\Demo01.txt");
        System.out.println(file2);
    }

}
