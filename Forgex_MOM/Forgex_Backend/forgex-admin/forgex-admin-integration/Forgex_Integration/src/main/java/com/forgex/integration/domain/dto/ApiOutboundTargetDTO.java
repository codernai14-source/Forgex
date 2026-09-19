package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口出站目标数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ApiOutboundTargetDTO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 接口配置 ID。
     */
    private Long apiConfigId;

    /**
     * 第三方系统 ID。
     */
    private Long thirdSystemId;

    /**
     * 目标编码。
     */
    private String targetCode;

    /**
     * 目标名称。
     */
    private String targetName;

    /**
     * 目标url。
     */
    private String targetUrl;

    /**
     * HTTP 请求方法。
     */
    private String httpMethod;

    /**
     * 内容类型。
     */
    private String contentType;

    /**
     * 调用模式。
     */
    private String invokeMode;

    /**
     * 超时时间（毫秒）。
     */
    private Integer timeoutMs;

    /**
     * 重试次数。
     */
    private Integer retryCount;

    /**
     * 重试间隔（毫秒）。
     */
    private Integer retryIntervalMs;

    /**
     * 排序号。
     */
    private Integer orderNum;

    /**
     * 状态。
     */
    private Integer status;

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
