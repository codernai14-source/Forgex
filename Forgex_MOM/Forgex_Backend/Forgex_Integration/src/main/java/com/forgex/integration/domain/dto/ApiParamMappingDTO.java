package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口参数映射信息 DTO
 * <p>
 * 用于服务层数据传输，包含参数映射的完整配置信息
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ApiParamMappingDTO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 接口配置表 ID（关联 fx_api_config 表）
     */
    private Long apiConfigId;

    /**
     * 源字段路径
     */
    private String sourceFieldPath;

    /**
     * 目标字段路径
     */
    private String targetFieldPath;

    /**
     * 转换规则（JSON 表达式或函数名）
     * 示例：toUpperCase(), formatDate('YYYY-MM-DD')
     */
    private String transformRule;

    /**
     * 映射方向：INBOUND-外对内，OUTBOUND-内调外
     */
    private String direction;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
