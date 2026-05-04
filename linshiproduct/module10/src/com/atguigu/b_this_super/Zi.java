package com.atguigu.b_this_super;

public class Zi extends Fu {
    int age ;

    public Zi() {
        super(1);
        System.out.println("我是子类无参构造");
    }

    public Zi(int age) {
        super(1);
        this.age = age;
    }
}
