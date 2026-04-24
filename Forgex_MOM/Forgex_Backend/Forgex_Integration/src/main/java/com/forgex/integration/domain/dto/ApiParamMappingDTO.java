package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口参数映射 DTO。
 */
@Data
public class ApiParamMappingDTO {

    private Long id;

    private Long apiConfigId;

    private String sourceFieldPath;

    private String targetFieldPath;

    private String transformRule;

    private String defaultValue;

    private String constantValue;

    private String targetScope;

    private String valueType;

    private String direction;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
