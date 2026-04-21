package com.forgex.common.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 授权组件自动配置
 * <p>
 * 为不同模块提供统一的机器码、授权文件读取和状态判定能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Configuration
@EnableConfigurationProperties({DeploymentProperties.class, LicenseProperties.class})
public class LicenseAutoConfiguration {

    /**
     * 创建机器指纹服务。
     *
     * @param licenseProperties License 配置属性
     * @return 机器指纹服务
     */
    @Bean
    public MachineFingerprintService machineFingerprintService(LicenseProperties licenseProperties) {
        return new MachineFingerprintService(licenseProperties);
    }

    /**
     * 创建 License 管理器。
     *
     * @param objectMapper 序列化组件
     * @param deploymentProperties 部署配置
     * @param licenseProperties 授权配置
     * @param machineFingerprintService 机器指纹服务
     * @return License 管理器
     */
    @Bean
    public LicenseManager licenseManager(
            ObjectMapper objectMapper,
            DeploymentProperties deploymentProperties,
            LicenseProperties licenseProperties,
            MachineFingerprintService machineFingerprintService
    ) {
        return new LicenseManager(objectMapper, deploymentProperties, licenseProperties, machineFingerprintService);
    }
}
