package com.atguigu.e_array;

public class Demo04Test {
    //定义数组 int[] nums = {5, 18, 23, 9, 31}，遍历数组并只打印偶数元素。
    static void main(String[] args) {
        int[] nums = {5, 18, 23, 9, 31};
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] % 2 == 0) {
                System.out.println(nums[i]);
            }
        }
    }
}
