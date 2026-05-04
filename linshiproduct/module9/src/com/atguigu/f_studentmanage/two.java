package com.atguigu.f_studentmanage;

import java.util.Scanner;

public class two {
    public static void print1() {
        Student[] students = new Student[100];
        int cout = 0;
        while (cout == 0) {
            System.out.println("-------------------------学生管理系统-------------------------");
            System.out.println("1 添加学生");
            System.out.println("2 修改学生");
            System.out.println("3 删除学生");
            System.out.println("4 查看学生");
            System.out.println("5 退出");
            System.out.print("请输入(1-5):");
            Scanner sc = new Scanner(System.in);
            int xuhao = sc.nextInt();
            if (xuhao == 1) {
                one.method(students);
            } else if (xuhao == 2) {
                one.method1(students);
            } else if (xuhao == 3) {
                one.method3(students);
            } else if (xuhao == 4) {
                one.method2(students);
            } else if (xuhao == 5) {
                System.out.println("是否退出？按9为退出，按0为取消。");
                int xuhao2 = sc.nextInt();
                if (xuhao2 == 9) {
                    System.out.println("bye");
                    cout++;
                }
                }
            else {
                    System.out.println("没有这个功能");
                }
            }
        }
    }

