package com.atguigu.b_polymorphic;

public class test {
    public static void main(String[] args) {
        Animal animal = new Animal();
        animal.method();

        Cat cat = new Cat();
        eat(cat);
    }


    public static void eat(Animal animal){
        animal.method();
    }
}
