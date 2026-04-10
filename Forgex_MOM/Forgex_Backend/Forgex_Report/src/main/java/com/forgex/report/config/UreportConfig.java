package com.forgex.report.config;

import org.springframework.context.annotation.Configuration;

/**
 * UReport2 报表配置类
 * <p>
 * 配置 UReport2 报表引擎的相关参数
 * UReport2 是一个轻量级的 Java 报表引擎，适合复杂中国式报表
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see com.pig4wei.ureport2.spring.boot3.starter.config.Ureport2AutoConfiguration
 */
@Configuration
public class UreportConfig {

    /**
     * UReport2 设计器访问路径前缀
     * <p>
     * 默认：/ureport
     * 访问示例：http://localhost:8084/ureport/designer?_url=classpath:templates/report/test.ureport.xml
     * </p>
     */
    public static final String DESIGNER_PATH = "/ureport";

    /**
     * UReport2 预览路径前缀
     * <p>
     * 默认：/ureport/preview
     * 访问示例：http://localhost:8084/ureport/preview?_url=classpath:templates/report/test.ureport.xml
     * </p>
     */
    public static final String PREVIEW_PATH = "/ureport/preview";

    /**
     * UReport2 导出路径前缀
     * <p>
     * 默认：/ureport/export
     * 支持导出格式：PDF、Excel、Word 等
     * </p>
     */
    public static final String EXPORT_PATH = "/ureport/export";

    /**
     * UReport2 数据源 API 路径
     * <p>
     * 用于配置报表数据源
     * </p>
     */
    public static final String DATASOURCE_API_PATH = "/api/report/ureport/datasource";
}
