package com.forgex.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;

/**
 * 认证服务启动类
 * 负责启动鉴权相关的微服务
 */
@SpringBootApplication(scanBasePackages = "com.forgex", exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@Import(DynamicDataSourceAutoConfiguration.class)
@MapperScan({"com.forgex.auth.mapper","com.forgex.common.mapper"})
public class ForgexAuthApplication {
    /**
     * 应用入口
     * @param args 启动参数
     * @see org.springframework.boot.SpringApplication#run(Class, String[])
     */
    public static void main(String[] args) {
        SpringApplication.run(ForgexAuthApplication.class, args);
    }
}
