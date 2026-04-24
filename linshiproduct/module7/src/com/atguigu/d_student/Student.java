package com.atguigu.d_student;

public class Student {
    String name;
    double chinese;
    double math;
    public void sum(){
        System.out.println(name+"的总成绩是"+(chinese+math));
    }
    public void average(){

        System.out.println(name+"的平均成绩是"+(chinese+math)/2);
    }
}
