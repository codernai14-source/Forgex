package com.atguigu.g_innerclass;

public class Test {
    public static void main(String[] args) {
        USB usb=new USB() {
            @Override
            public void open() {
                System.out.println("USB打开了");
            }
        };
        usb.open();
    }
}
