package com.atguigu.a_array;

public class Demo07Array {
    static void main(String[] args) {
        int[] arr=new int[3];
        System.out.println(arr);
        System.out.println(arr[0]);
        System.out.println(arr[1]);
        System.out.println(arr[2]);
        //cunshuju
        arr[0]=100;
        arr[1]=200;
        arr[2]=300;
        System.out.println(arr[0]);
        System.out.println(arr[1]);
        System.out.println(arr[2]);
        String[] arr2=new String[3];
        System.out.println(arr2);
        System.out.println(arr2[0]);
        System.out.println(arr2[1]);
        System.out.println(arr2[2]);
        arr2[0]="今天";
        arr2[1]="zuo天";
        arr2[2]="ming天";
        System.out.println(arr2[0]);
        System.out.println(arr2[1]);
        System.out.println(arr2[2]);
        System.out.println("===========遍历arr数组===============");

        for (int i=0;i<arr.length;i++){
            System.out.println(i);

        }
        System.out.println("===========遍历arr2数组===============");
        //kuaijiejian 数组名.for.i
        for (int i = 0; i < arr2.length; i++) {

        }

    }
}
