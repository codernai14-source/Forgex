package com.forgex.report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 报表配置类
 * <p>
 * 用于配置报表模块的通用参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Configuration
@ConfigurationProperties(prefix = "forgex.report")
public class ReportConfig {

    /**
     * 模板文件存储路径
     */
    private String templatePath = "templates/report";

    /**
     * 是否启用 UReport2
     */
    private boolean enableUreport = true;

    /**
     * 是否启用 JimuReport
     */
    private boolean enableJimu = true;

    /**
     * 模板文件最大大小（MB）
     */
    private Integer maxFileSize = 10;

    /**
     * 获取模板文件存储路径
     *
     * @return 模板文件存储路径
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * 设置模板文件存储路径
     *
     * @param templatePath 模板文件存储路径
     */
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * 是否启用 UReport2
     *
     * @return 是否启用
     */
    public boolean isEnableUreport() {
        return enableUreport;
    }

    /**
     * 设置是否启用 UReport2
     *
     * @param enableUreport 是否启用
     */
    public void setEnableUreport(boolean enableUreport) {
        this.enableUreport = enableUreport;
    }

    /**
     * 是否启用 JimuReport
     *
     * @return 是否启用
     */
    public boolean isEnableJimu() {
        return enableJimu;
    }

    /**
     * 设置是否启用 JimuReport
     *
     * @param enableJimu 是否启用
     */
    public void setEnableJimu(boolean enableJimu) {
        this.enableJimu = enableJimu;
    }

    /**
     * 获取模板文件最大大小
     *
     * @return 最大大小（MB）
     */
    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * 设置模板文件最大大小
     *
     * @param maxFileSize 最大大小（MB）
     */
    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}
