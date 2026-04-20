package com.forgex.report.config;

import org.springframework.context.annotation.Configuration;

/**
 * JimuReport 积木报表配置类
 * <p>
 * 配置 JimuReport 报表引擎的相关参数
 * JimuReport 是一个功能丰富的低代码报表引擎，支持数据可视化大屏
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see org.jeecgframework.jimureport.spring.boot3.starter.config.JimuReportAutoConfiguration
 */
@Configuration
public class JimuConfig {

    /**
     * JimuReport 设计器访问路径前缀
     * <p>
     * 默认：/jmreport
     * 访问示例：http://localhost:8084/jmreport/designer?id=123
     * </p>
     */
    public static final String DESIGNER_PATH = "/jmreport";

    /**
     * JimuReport 预览路径前缀
     * <p>
     * 默认：/jmreport/preview
     * 访问示例：http://localhost:8084/jmreport/preview?id=123
     * </p>
     */
    public static final String PREVIEW_PATH = "/jmreport/preview";

    /**
     * JimuReport 导出路径前缀
     * <p>
     * 默认：/jmreport/export
     * 支持导出格式：PDF、Excel、Word 等
     * </p>
     */
    public static final String EXPORT_PATH = "/jmreport/export";

    /**
     * JimuReport 大屏访问路径前缀
     * <p>
     * 默认：/jmreport/bigScreen
     * 用于数据可视化大屏展示
     * </p>
     */
    public static final String BIG_SCREEN_PATH = "/jmreport/bigScreen";

    /**
     * JimuReport API 路径前缀
     * <p>
     * 默认：/jmreport/api
     * 用于报表保存、加载等 API 调用
     * </p>
     */
    public static final String API_PATH = "/jmreport/api";
}
