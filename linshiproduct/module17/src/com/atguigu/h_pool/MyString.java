package com.atguigu.h_pool;

import java.util.concurrent.Callable;

public class MyString implements Callable<String> {
    @Override
    public String call() throws Exception {
        return "i love java";
    }
}
