package com.forgex.integration.domain.param;

import lombok.Data;

/**
 * 接口参数配置查询参数类
 * <p>
 * 用于接收前端传递的查询条件
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ApiParamConfigParam {

    /**
     * 接口配置 ID
     */
    private Long apiConfigId;

    /**
     * 出站目标 ID。
     */
    private Long outboundTargetId;

    /**
     * 参数方向：REQUEST-请求，RESPONSE-响应
     */
    private String direction;

    /**
     * 父节点 ID（用于查询子节点）
     */
    private Long parentId;

    /**
     * 节点类型：OBJECT-集合，ARRAY-数组，FIELD-字段
     */
    private String nodeType;
}
