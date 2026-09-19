package com.company.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 独立业务入口，仅扫描公司的业务包，不扫描平台实现。 */
@SpringBootApplication
public class CompanyOrderApplication {

    /**
     * 启动企业订单服务。
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CompanyOrderApplication.class, args);
    }
}
