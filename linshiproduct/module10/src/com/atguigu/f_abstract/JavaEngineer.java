package com.atguigu.f_abstract;

public class JavaEngineer extends Rdd {
    public JavaEngineer() {
    }

    public JavaEngineer(int employeeId, String employeeName) {
        super(employeeId, employeeName);
    }

    @Override
    public void work() {
        System.out.println("员工号为"+getEmployeeName()+ "的"+getEmployeeId()+"员工正在研发电商网站");
    }
}
