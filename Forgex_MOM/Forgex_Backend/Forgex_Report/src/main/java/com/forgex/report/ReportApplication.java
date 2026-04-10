package com.forgex.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 报表服务启动类
 * <p>
 * 集成 UReport2 和 JimuReport 双报表引擎
 * 提供报表设计、管理、预览服务
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see SpringApplication
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.forgex.report", "com.forgex.common"})
@MapperScan("com.forgex.report.mapper")
public class ReportApplication {

    /**
     * 主方法，启动 Spring Boot 应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);
    }
}
