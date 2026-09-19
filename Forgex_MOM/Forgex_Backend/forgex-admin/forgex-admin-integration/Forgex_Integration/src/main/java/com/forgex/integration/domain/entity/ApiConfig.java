package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口配置实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_config")
public class ApiConfig extends BaseEntity {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * INBOUND / OUTBOUND
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
     * HTTP / TCP
     */
    private String callMethod;

    /**
     * HTTP 请求方法。
     */
    private String httpMethod;

    /**
     * SYNC / ASYNC
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
     * NONE / TOKEN / BASIC / CUSTOM
     */
    private String authType;

    /**
     * JSON config
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
}
