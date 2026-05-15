package com.atguigu.a_string;

import java.util.Scanner;

public class Demo03 {
    public static void main(String[] args) {
        String user="root";
        String pwd="123";

        for (int i=0;i<3;i++){
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入账号");
            String user1 = sc.next();

            System.out.println("请输入密码");
            String pwd1 = sc.next();
            if (user.equals(user1)&&pwd.equals(pwd1)){
                System.out.println("登陆成功");
                break;
            }else {
                if (i==2){
                    System.out.println("账号冻结");
                }else {
                    System.out.println("登陆失败");
                }
            }
        }

    }
}
