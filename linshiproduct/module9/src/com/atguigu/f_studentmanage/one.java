package com.atguigu.f_studentmanage;

import java.util.Scanner;

public class one {
    static Student[] students=new Student[100];
    static int count = 0;

    //添加功能
     public static void method(Student[] students) {

         boolean kongzhiqi =true;
         boolean kongzhi2 = false;
         while (kongzhiqi){
             // 用户要先输入学号
             Student student = new Student();
             Scanner sc = new Scanner(System.in);
             System.out.println("请输入学号");
             student.setNum(sc.nextInt());
             // 要判断学号是否重复
             for (int i = 0; i < count; i++) {
                 if(one.students[i].getNum() == student.getNum() ){
                     System.out.println("学号重复！");
                     kongzhi2 = true;
                     break;
                 }else {
                     kongzhi2 =false;
                 }
             }
             if(kongzhi2){
                 continue;
             }
             // 再输入姓名
             Scanner sc1 = new Scanner(System.in);
             System.out.println("请输入姓名");
             student.setName(sc1.next());
             // 再输入年龄
             Scanner sc2 = new Scanner(System.in);
             System.out.println("请输入年龄");
             student.setOld(sc2.nextInt());
             // 在输入性别
             Scanner sc3 = new Scanner(System.in);
             System.out.println("请输入性别");
             student.setGender(sc3.next());
             // 再输入班级
             Scanner sc4 = new Scanner(System.in);
             System.out.println("请输入班级");
             student.setClassroom(sc4.nextInt());
             // 对数组对象进行新增操作
             one.students[count] = student;
             count++;
             //提示添加成功，是否继续添加，如果不继续添加，退回到菜单
             System.out.println("添加成功！是否继续添加，0否，1是");
             Scanner sc5 = new Scanner(System.in);
             int xuanze = sc5.nextInt();
             if(xuanze != 1){
                 kongzhiqi = false;
             }
         }



//
//         Scanner sc = new Scanner(System.in);
//         System.out.println("请输入学号");
//         int num = sc.nextInt();
//         for (int j = 0; j < 1; j++) {
//             if (students!=null||students[j].getNum() == num) {
//                 System.out.println("学号重复，请重新输入");
//                 method(students);
//                 return;
//
//             }
//             Student student = new Student();
//                student.setNum(num);
//                 System.out.println("请输入班级");
//                 int classroom = sc.nextInt();
//                 System.out.println("请输入姓名");
//                 String name = sc.next();
//                 System.out.println("请输入性别");
//                 String gender = sc.next();
//                 student.setClassroom(classroom);
//                 student.setName(name);
//                 student.setGender(gender);
//                 students[count] = student;
//                 count++;
//             System.out.println("添加成功");
//             }
     }




         //修改功能
    public static void method1(Student[] students){
         boolean kzq =true;
         boolean kzq2 =false;
         while (kzq) {

             //1.当前没有学生
             if (count == 0) {
                 System.out.println("当前暂无学生");
                 break;
             }else {
                 //输入要修改的学号
                 Scanner sc1 = new Scanner(System.in);
                 System.out.println("请输入要修改的学生学号");
                 int num1 = sc1.nextInt();
                 //查找该学号对应的学生对象
                 for (int i = 0; i < count; i++) {
                     if(one.students[i].getNum()==num1){
                         //输入修改后的姓名
                         System.out.println("请输入修改后的姓名");
                         Scanner sc2 =new Scanner(System.in);
                         String name = sc1.next();
                         one.students[i].setName(name);
                         //输入修改后的年龄
                         System.out.println("请输入修改后的年龄");
                         Scanner sc3 =new Scanner(System.in);
                         int old = sc3.nextInt();
                         one.students[i].setOld(old);
                         //输入修改后的性别
                         System.out.println("请输入修改后的性别");
                         Scanner sc4 =new Scanner(System.in);
                         String gender = sc3.next();
                         one.students[i].setGender(gender);
                         //输入修改后的班级
                         System.out.println("请输入修改后的班级");
                         Scanner sc5 =new Scanner(System.in);
                         int classroom = sc3.nextInt();
                         one.students[i].setClassroom(classroom);
                         //修改成功提示，结束循环
                         System.out.println("修改成功！");
                         kzq2=true;
                         break;
                     }else {
                         //没有查找到该学号，让用户重新输入
                         System.out.println("请输入正确的学号");

                     }
                 }
                 if(kzq2){
                     break;
                 }
             }
         }
    }
//         public static void method1 (Student[]students) {
//             System.out.println("请输入要修改的学生学号");
//             Scanner sc = new Scanner(System.in);
//             int num = sc.nextInt();
//             int count1=0;
//             for (int j = 0; j < count; j++) {
//                 if (students!=null||students[j].getNum() != num) {
//                     count1++;
//                 }
//             }
//             if (count1==count){
//                 System.out.println("查无此人，请重新输入");
//                 method1(students);
//
//             }
//                     System.out.println("请输入班级");
//                     int classroom = sc.nextInt();
//                     students[num].setClassroom(classroom);
//                     System.out.println("请输入姓名");
//                     String name = sc.next();
//                     students[num].setName(name);
//                     System.out.println("请输入性别");
//                     String gender = sc.next();
//                     students[num].setGender(gender);
//
//
//
//         }
                 //查看功能
                 public static void method2 (Student[] students){
                        if (count==0) {
                            System.out.println("当前暂无学生，待开班");
                            return;
                        }
                         System.out.println("学号 班级 姓名 性别");
                     for (int i = 0; i < count; i++) {
                         System.out.print(one.students[i].getNum());
                         System.out.print("    ");
                         System.out.print(one.students[i].getClassroom());
                         System.out.print("   ");
                         System.out.print(one.students[i].getName());
                         System.out.print("   ");
                         System.out.println(one.students[i].getGender());

                     }


                 }
                 //删除功能
                 public static void method3 (Student[]students){
                    //当前未有学生的情况
                     if (count==0) {
                         System.out.println("当前暂无学生");
                         return;
                     }
                     boolean kzq = true;
                     boolean kzq2 = false;
                     while (kzq) {
                         //输入要删除的学生学号
                         Scanner sc = new Scanner(System.in);
                         System.out.println("请输入要删除学生的学号");
                         int num = sc.nextInt();
                         //找到该学生对象
                         for (int i = 0; i < count; i++) {
                             if (one.students[i].getNum() == num) {
                                 //当删除的是最后一个学生
                                 if (i == count) {
                                     one.students[i] = null;
                                     System.out.println("删除成功");
                                     count--;
                                     break;
                                     //当删除的不是最后一个学生
                                 } else {
                                     for (int j = i; j < count - 1; j++) {
                                         one.students[j] = one.students[j + 1];
                                     }
                                     one.students[count] = null;
                                     System.out.println("删除成功");
                                     count--;
                                 }
                                 kzq2=true;
                                 break;
                                 //没有找到该学号的对象
                             } else {
                                 System.out.println("查无此人，请重新输入");
                             }


                         }
                         if (kzq2){
                             break;
                         }
                     }
//                     for (int i = 0; i < count; i++) {
//                         if (a==students[i].getNum()){
//                             for (int j = a; j < count-1; j++) {
//                                 students[j]=students[j+1];
//                             }
//                         }
//                         else {
//                             System.out.println("查无此人，请重新输入");
//                             method3(students);
//                         }
//                     }

                 }


             }


