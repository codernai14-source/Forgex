package com.atguigu.f_callable;

import java.util.concurrent.Callable;

public class MyCallable implements Callable {
    @Override
    public String call() throws Exception {
        return "我爱Java";
    }
}
