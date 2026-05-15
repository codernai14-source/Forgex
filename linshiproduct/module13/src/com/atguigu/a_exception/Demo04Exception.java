package com.atguigu.a_exception;

import java.util.Scanner;

public class Demo04Exception {
    public static void main(String[] args)throws LoginuserException {
        String username ="root";
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入用户名");
        String name= sc.next();
        if (name.equals(username)){
            System.out.println("登陆成功");
        }else {
            try {
            throw new LoginuserException("账号或密码错误");

        }catch (LoginuserException loginuserException){
                //loginuserException.printStackTrace();
               // System.out.println(loginuserException.getMessage());
                System.out.println(loginuserException);
        }
        }

    }
}
