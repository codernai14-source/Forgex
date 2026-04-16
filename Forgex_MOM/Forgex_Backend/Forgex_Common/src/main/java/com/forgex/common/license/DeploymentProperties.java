package com.forgex.common.license;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 部署环境配置属性
 * <p>
 * 统一承载安装器和部署脚本注入的目录、实例编码与基础连接信息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "forgex.deployment")
public class DeploymentProperties {

    /**
     * 部署实例编码。
     */
    private String instanceCode = "DEFAULT";

    /**
     * 当前部署环境标识。
     */
    private String profile = "dev";

    /**
     * Forgex 实例根目录。
     */
    private String home = "./forgex";

    /**
     * 授权目录。
     */
    private String licenseDir = "./forgex/license";

    /**
     * 上传目录。
     */
    private String uploadDir = "./forgex/data/uploads";

    /**
     * 日志目录。
     */
    private String logDir = "./forgex/logs";

    /**
     * 备份目录。
     */
    private String backupDir = "./forgex/backup";

    /**
     * Nacos 地址。
     */
    private String nacosAddr = "127.0.0.1:8848";

    /**
     * Redis 地址。
     */
    private String redisAddr = "127.0.0.1:6379";

    /**
     * MySQL JDBC 地址。
     */
    private String mysqlUrl = "";
}
