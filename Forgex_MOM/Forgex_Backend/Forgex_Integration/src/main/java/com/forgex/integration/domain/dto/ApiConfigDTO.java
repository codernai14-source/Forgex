package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口配置 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ApiConfigDTO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 接口编码。
     */
    private String apiCode;

    /**
     * 接口名称。
     */
    private String apiName;

    /**
     * 接口desc。
     */
    private String apiDesc;

    /**
     * 方向。
     */
    private String direction;

    /**
     * 接口路径。
     */
    private String apiPath;

    /**
     * 处理器 Bean 名称。
     */
    private String processorBean;

    /**
     * 调用method。
     */
    private String callMethod;

    /**
     * HTTP 请求方法。
     */
    private String httpMethod;

    /**
     * 调用模式。
     */
    private String invokeMode;

    /**
     * 内容类型。
     */
    private String contentType;

    /**
     * 目标url。
     */
    private String targetUrl;

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
     * 最大并发数。
     */
    private Integer maxConcurrent;

    /**
     * 队列上限。
     */
    private Integer queueLimit;

    /**
     * 认证类型。
     */
    private String authType;

    /**
     * 认证配置。
     */
    private String authConfig;

    /**
     * 调用count。
     */
    private Long callCount;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 出站targets。
     */
    private List<ApiOutboundTargetDTO> outboundTargets;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
