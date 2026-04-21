package com.forgex.common.license;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 部署环境配置属性。
 * <p>
 * 统一承载安装器和部署脚本注入的目录、实例编码与基础连接信息。
 * 在本地开发场景下，会将默认相对路径归一到仓库根目录，避免从不同模块启动时生成多份运行目录。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "forgex.deployment")
public class DeploymentProperties {

    private static final String DEFAULT_HOME = "./forgex";
    private static final String DEFAULT_LICENSE_DIR = "./forgex/license";
    private static final String DEFAULT_UPLOAD_DIR = "./forgex/data/uploads";
    private static final String DEFAULT_LOG_DIR = "./forgex/logs";
    private static final String DEFAULT_BACKUP_DIR = "./forgex/backup";

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
    private String home = DEFAULT_HOME;

    /**
     * 授权目录。
     */
    private String licenseDir = DEFAULT_LICENSE_DIR;

    /**
     * 上传目录。
     */
    private String uploadDir = DEFAULT_UPLOAD_DIR;

    /**
     * 日志目录。
     */
    private String logDir = DEFAULT_LOG_DIR;

    /**
     * 备份目录。
     */
    private String backupDir = DEFAULT_BACKUP_DIR;

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

    /**
     * 归一化运行目录。
     */
    @PostConstruct
    public void normalizePaths() {
        Path workspaceRoot = detectWorkspaceRoot();
        home = normalizePath(home, workspaceRoot, DEFAULT_HOME);
        licenseDir = normalizePath(licenseDir, workspaceRoot, DEFAULT_LICENSE_DIR);
        uploadDir = normalizePath(uploadDir, workspaceRoot, DEFAULT_UPLOAD_DIR);
        logDir = normalizePath(logDir, workspaceRoot, DEFAULT_LOG_DIR);
        backupDir = normalizePath(backupDir, workspaceRoot, DEFAULT_BACKUP_DIR);
    }

    private Path detectWorkspaceRoot() {
        Path current = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path cursor = current;
        while (cursor != null) {
            boolean hasBackendMarker = Files.isDirectory(cursor.resolve("Forgex_MOM"));
            boolean hasBuildMarker = Files.isDirectory(cursor.resolve("Forgex_Build"));
            if (hasBackendMarker && hasBuildMarker) {
                return cursor;
            }
            if (Files.isDirectory(cursor.resolve(".git"))) {
                return cursor;
            }
            cursor = cursor.getParent();
        }
        return current;
    }

    private String normalizePath(String rawValue, Path workspaceRoot, String defaultValue) {
        String value = rawValue;
        if (value == null || value.isBlank()) {
            value = defaultValue;
        }
        Path path = Path.of(value);
        if (path.isAbsolute()) {
            return path.normalize().toString();
        }
        return workspaceRoot.resolve(path).normalize().toString();
    }
}
