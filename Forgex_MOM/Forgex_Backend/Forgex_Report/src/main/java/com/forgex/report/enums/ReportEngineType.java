package com.forgex.report.enums;

/**
 * 报表引擎类型枚举
 * <p>
 * 定义支持的报表引擎类型：UReport2 和 JimuReport
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see com.forgex.report.domain.entity.ReportTemplate
 */
public enum ReportEngineType {
    
    /**
     * UReport2 报表引擎
     * <p>
     * 轻量级报表引擎，适合复杂中国式报表、票据打印、数据导出
     * </p>
     */
    UREPORT("ureport", "UReport2"),
    
    /**
     * JimuReport 积木报表引擎
     * <p>
     * 功能丰富的低代码报表引擎，支持数据可视化大屏、BI 报表、仪表盘
     * </p>
     */
    JIMU("jimu", "JimuReport");
    
    /**
     * 引擎编码
     */
    private final String code;
    
    /**
     * 引擎名称
     */
    private final String name;
    
    /**
     * 构造函数
     *
     * @param code 引擎编码
     * @param name 引擎名称
     */
    ReportEngineType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 获取引擎编码
     *
     * @return 引擎编码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取引擎名称
     *
     * @return 引擎名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 根据编码获取枚举值
     *
     * @param code 引擎编码
     * @return 报表引擎类型枚举
     * @throws IllegalArgumentException 当编码不合法时抛出
     */
    public static ReportEngineType fromCode(String code) {
        for (ReportEngineType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的报表引擎类型：" + code);
    }
}
