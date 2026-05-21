package com.atguigu.b_utils;

public class Demo01System {
    public static void main(String[] args) {
        //currentTimeMillis();
        //exit();
        arraycopy();
    }

    private static void arraycopy() {
        int[] arr={1,2,3,4,5};
        int[] arr2=new int[10];
        System.arraycopy(arr,0,arr2,0,5);
        for (int i = 0; i < arr2.length; i++) {
            System.out.print(arr2[i]+" ");
        }
    }

    private static void exit() {
        for (int i=0;i<100;i++){
            if (i==5){
                System.exit(0);
            }
            System.out.println("hello world"+i);
        }
    }

    private static void currentTimeMillis() {
        long l = System.currentTimeMillis();
        System.out.println(l);
    }
}
