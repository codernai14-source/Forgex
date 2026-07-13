package com.atguigu.b_genericity;

import java.util.Arrays;

public class MyArryList <E>{
    Object[] object=new Object[10];
    int size;

    //add method
    public boolean add(E e){
        object[size]=e;
        size++;
        return true;
    }

    public E get(int index){
        return (E)object[index];

    }

    @Override
    public String toString() {
        return Arrays.toString(object);
    }
}
