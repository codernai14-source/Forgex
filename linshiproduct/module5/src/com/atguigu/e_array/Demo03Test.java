package com.atguigu.e_array;

public class Demo03Test {
    static void main(String[] args) {
        //定义二维数组 int[][] arr = {{1,2,3},{4,5},{6,7,8,9}}，使用嵌套循环完成二维数组的全部元素遍历打印;
        int[][] arr ={{1,2,3},{4,5},{6,7,8,9}};
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                System.out.println(arr[i][j]);
            }
        }
        }
    }

