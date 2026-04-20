package com.atguigu.e_array;

public class Demo06Test {
    //定义二维数组 int[][] arr = {{10,20},{30,40,50},{60}}，嵌套遍历并求和所有元素，最终输出总和。
    static void main(String[] args) {
        int[][] arr = {{10,20},{30,40,50},{60}};
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                sum+=arr[i][j];
            }
        }
        System.out.println(sum);
    }
}
