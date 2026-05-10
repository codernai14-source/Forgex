package com.atguigu.e_innerclass;

public class Person {
    static class Heart{
        public void jump(){
            System.out.println("心脏在哐哐跳动");
        }

    }
     class Lung{
        public void work(){
            System.out.println("肺部正在正常呼吸");
        }
    }
}
