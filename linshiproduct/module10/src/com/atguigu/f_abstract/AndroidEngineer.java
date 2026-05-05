package com.atguigu.f_abstract;

public class AndroidEngineer extends Rdd {
    public AndroidEngineer() {
    }

    public AndroidEngineer(int employeeId, String employeeName) {
        super(employeeId, employeeName);
    }

    @Override
    public void work() {
        System.out.println("员工号为"+getEmployeeName()+ "的"+getEmployeeId()+"员工正在研发电商手机客户端软件");
    }
}
