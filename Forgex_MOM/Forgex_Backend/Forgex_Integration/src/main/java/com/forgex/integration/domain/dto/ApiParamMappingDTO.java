package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口参数映射 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ApiParamMappingDTO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 接口配置 ID。
     */
    private Long apiConfigId;

    /**
     * 出站目标 ID。
     */
    private Long outboundTargetId;

    /**
     * 来源字段路径。
     */
    private String sourceFieldPath;

    /**
     * 目标字段路径。
     */
    private String targetFieldPath;

    /**
     * 转换规则。
     */
    private String transformRule;

    /**
     * 默认价值。
     */
    private String defaultValue;

    /**
     * 常量值。
     */
    private String constantValue;

    /**
     * 目标scope。
     */
    private String targetScope;

    /**
     * 价值类型。
     */
    private String valueType;

    /**
     * 方向。
     */
    private String direction;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
