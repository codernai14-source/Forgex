package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口配置信息实体类
 * <p>
 * 对应数据库表：fx_api_config
 * 用于存储接口的基础配置信息，包括接口编码、路径、处理器等
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_config")
public class ApiConfig extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接口编码（唯一标识）
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
     * 接口路径（外对内时必填）
     */
    private String apiPath;

    /**
     * 处理器 bean 名称（外对内时必填）
     * 用于 Spring 容器查找对应的业务处理器
     */
    private String processorBean;

    /**
     * 调用方式：HTTP, TCP
     */
    private String callMethod;

    /**
     * 目标地址（内调外时填写）
     */
    private String targetUrl;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeoutMs;

    /**
     * 调用次数（累计值）
     */
    private Long callCount;

    /**
     * 状态：0-禁用，1-启用
     * @see com.forgex.common.constant.SystemConstants#STATUS_DISABLED
     * @see com.forgex.common.constant.SystemConstants#STATUS_NORMAL
     */
    private Integer status;

    /**
     * 所属模块编码
     */
    private String moduleCode;
}
