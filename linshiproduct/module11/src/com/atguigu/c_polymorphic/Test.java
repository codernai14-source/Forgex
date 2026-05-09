package com.atguigu.c_polymorphic;

public class Test {
    public static void show(Animal animal){
        animal.eat();
        if (animal instanceof Cat cat){
            cat.catchMouse();
        }else if (animal instanceof Dog dog){
            dog.watchHouse();
        }

    }

    public static void main(String[] args) {
        show(new Cat());
        show(new Dog());

    }
}
