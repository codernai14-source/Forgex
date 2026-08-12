package com.forgex.integration;

import com.forgex.common.api.feign.AuthPermClient;
import com.forgex.common.api.feign.IntegrationEmployeeSyncFeignClient;
import com.forgex.common.api.feign.IntegrationMaterialSyncFeignClient;
import com.forgex.common.api.feign.IntegrationSupplierSyncFeignClient;
import com.forgex.common.api.feign.IntegrationUserSyncFeignClient;
import com.forgex.common.security.perm.PermissionInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

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
@Slf4j
@SpringBootApplication(scanBasePackages = "com.forgex")
@EnableDiscoveryClient
@EnableAsync
@EnableFeignClients(clients = {
        AuthPermClient.class,
        IntegrationEmployeeSyncFeignClient.class,
        IntegrationUserSyncFeignClient.class,
        IntegrationSupplierSyncFeignClient.class,
        IntegrationMaterialSyncFeignClient.class
})
@Import(PermissionInterceptor.class)
@MapperScan({"com.forgex.integration.mapper", "com.forgex.common.mapper"})
public class ForgexIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForgexIntegrationApplication.class, args);
        log.info("========================================");
        log.info("Forgex Integration Service Started Successfully!");
        log.info("========================================");
    }
}
