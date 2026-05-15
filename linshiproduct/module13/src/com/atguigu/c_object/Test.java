package com.atguigu.c_object;

public class Test {
    public static void main(String[] args) {
        Person[] person=new Person[3];
        Person person1 = new Person(14, "111");
        Person person2 = new Person(18, "111");
        Person person3 = new Person(11, "111");
        person[0]=person1;
        person[1]=person2;
        person[2]=person3;
        for (int j = 0; j < person.length-1; j++) {
            for (int i = 0; i < person.length-1-j; i++) {
                if (person[i].compareTo(person[i+1])>0){
                    Person temp= person[i];
                    person[i]=person[i+1];
                    person[i+1]=temp;
                }
            }

        }
        for (int i = 0; i < person.length; i++) {
            System.out.println(person[i]);
        }

    }
}
