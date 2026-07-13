package com.atguigu.b_genericity;

public class Demo04 {
    public static void main(String[] args) {
        MyArryList1<String> list1 = new MyArryList1<>();
        list1.add("zhangsan");
        list1.add("zhangsi");
        System.out.println(list1);
    }
}
