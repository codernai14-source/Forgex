package com.forgex.basic.label.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 标签模板配置类
 * <p>
 * 管理标签打印模块的配置参数，支持从 application.yml 或 Nacos 读取配置
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Component
@ConfigurationProperties(prefix = "forgex.label")
public class LabelTemplateConfig {

    /**
     * 是否启用标签打印模块
     */
    private Boolean enabled = true;

    /**
     * 默认打印机名称
     */
    private String defaultPrinter = "ZEBRA_ZD888";

    /**
     * 默认打印机 IP 地址
     */
    private String defaultPrinterIp = "192.168.1.100";

    /**
     * 单次最大打印张数
     */
    private Integer maxPrintCount = 1000;

    /**
     * 补打最大次数限制
     */
    private Integer maxReprintCount = 10;

    /**
     * 条码号前缀
     */
    private String barcodePrefix = "BC";

    /**
     * 条码号长度（不含前缀）
     */
    private Integer barcodeLength = 20;

    /**
     * 打印超时时间（秒）
     */
    private Integer printTimeout = 30;

    /**
     * 是否启用打印预览
     */
    private Boolean enablePreview = true;

    /**
     * 是否启用打印日志
     */
    private Boolean enablePrintLog = true;

    /**
     * 是否启用异常记录
     */
    private Boolean enableExceptionRecord = true;

    /**
     * 模板缓存过期时间（分钟）
     */
    private Integer templateCacheExpire = 60;

    /**
     * 是否启用版本控制
     */
    private Boolean enableVersionControl = true;

    /**
     * 最大版本保留数量（0表示不限制）
     */
    private Integer maxVersionKeep = 0;

    /**
     * 默认模板类型
     */
    private String defaultTemplateType = "GENERAL";

    /**
     * 支持的模板类型列表
     */
    private String[] supportedTemplateTypes = {
            "INCOMING", "SEMI_FINISHED", "FINISHED",
            "OUTBOUND", "RETURN", "DEFECTIVE",
            "INVENTORY", "GENERAL"
    };

    /**
     * 条码生成策略：TIMESTAMP（时间戳）、SEQUENCE（序列号）、BUSINESS（业务规则）
     */
    private String barcodeGenerateStrategy = "TIMESTAMP";

    /**
     * 是否校验模板 JSON 格式
     */
    private Boolean validateTemplateJson = true;

    /**
     * 打印数据快照保留天数
     */
    private Integer snapshotRetentionDays = 365;

    /**
     * ZPL 打印机配置
     */
    private ZplPrinterConfig zplPrinter = new ZplPrinterConfig();

    /**
     * TSPL 打印机配置
     */
    private TsplPrinterConfig tsplPrinter = new TsplPrinterConfig();

    /**
     * ZPL 打印机配置
     */
    @Data
    public static class ZplPrinterConfig {
        /**
         * 默认标签宽度（mm）
         */
        private Integer defaultWidth = 100;

        /**
         * 默认标签高度（mm）
         */
        private Integer defaultHeight = 150;

        /**
         * 默认打印浓度（0-30）
         */
        private Integer defaultDensity = 15;

        /**
         * 默认打印速度（英寸/秒）
         */
        private Integer defaultSpeed = 4;
    }

    /**
     * TSPL 打印机配置
     */
    @Data
    public static class TsplPrinterConfig {
        /**
         * 默认标签宽度（mm）
         */
        private Integer defaultWidth = 100;

        /**
         * 默认标签高度（mm）
         */
        private Integer defaultHeight = 150;

        /**
         * 默认打印浓度（0-15）
         */
        private Integer defaultDensity = 8;

        /**
         * 默认打印速度（mm/s）
         */
        private Integer defaultSpeed = 100;
    }
}
