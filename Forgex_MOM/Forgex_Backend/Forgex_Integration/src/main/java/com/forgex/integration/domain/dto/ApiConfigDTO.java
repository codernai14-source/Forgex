package com.forgex.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口配置信息 DTO
 * <p>
 * 用于服务层数据传输
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ApiConfigDTO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 接口描述
     */
    private String apiDesc;

    /**
     * 操作方向：INBOUND-外对内，OUTBOUND-内调外
     */
    private String direction;

    /**
     * 接口路径
     */
    private String apiPath;

    /**
     * 处理器 bean 名称
     */
    private String processorBean;

    /**
     * 调用方式：HTTP, TCP
     */
    private String callMethod;

    /**
     * 目标地址
     */
    private String targetUrl;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeoutMs;

    /**
     * 调用次数
     */
    private Long callCount;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 所属模块编码
     */
    private String moduleCode;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
