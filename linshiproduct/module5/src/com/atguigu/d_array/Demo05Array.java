package com.atguigu.d_array;

public class Demo05Array {
    static void main(String[] args) {
        String[][] arr = new String[2][2];
        arr[0][0]="园丁";
        arr[0][1]="园丁2";
        arr[1][0]="园丁3";
        arr[1][1]="园丁4";
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                System.out.println(arr[i][j]);
            }
        }
    }
}
