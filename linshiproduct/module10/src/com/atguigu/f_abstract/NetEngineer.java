package com.atguigu.f_abstract;

public class NetEngineer extends Md {
    public NetEngineer() {
    }

    public NetEngineer(int employeeId, String employeeName) {
        super(employeeId, employeeName);
    }

    @Override
    public void work() {
        System.out.println("员工号为"+getEmployeeName()+ "的"+getEmployeeId()+"员工正在正在检修网络是否通畅");
    }
}
