package com.forgex.integration.domain.param;

import lombok.Data;

/**
 * 接口参数映射查询参数类
 * <p>
 * 用于接收前端传递的查询条件
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ApiParamMappingParam {

    /**
     * 接口配置 ID
     */
    private Long apiConfigId;

    /**
     * 出站目标 ID。
     */
    private Long outboundTargetId;

    /**
     * 映射方向：INBOUND-外对内，OUTBOUND-内调外
     */
    private String direction;

    /**
     * 源字段路径（模糊查询）
     */
    private String sourceFieldPath;

    /**
     * 目标字段路径（模糊查询）
     */
    private String targetFieldPath;
}
