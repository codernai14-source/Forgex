package com.atguigu.h_pool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(2);
        Future<String> submit = es.submit(new MyString());
        Future<Integer> submit1 = es.submit(new MySum());
        System.out.println(submit.get());
        System.out.println(submit1.get());
    }
}
