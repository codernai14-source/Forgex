package com.forgex.integration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 接口平台服务启动类
 * <p>
 * 负责启动接口平台服务，提供第三方系统管理、接口配置、参数映射、调用记录等功能
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.forgex.integration.mapper")
public class ForgexIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForgexIntegrationApplication.class, args);
        System.out.println("========================================");
        System.out.println("Forgex Integration Service Started Successfully!");
        System.out.println("========================================");
    }
}
