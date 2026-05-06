package com.atguigu.f_abstract;

public class HardwareEngineer extends Md {
    public HardwareEngineer() {
    }

    public HardwareEngineer(int employeeId, String employeeName) {
        super(employeeId, employeeName);
    }

    @Override
    public void work() {
        System.out.println("员工号为"+getEmployeeName()+ "的"+getEmployeeId()+"员工正在修复电脑主板");
    }
}
