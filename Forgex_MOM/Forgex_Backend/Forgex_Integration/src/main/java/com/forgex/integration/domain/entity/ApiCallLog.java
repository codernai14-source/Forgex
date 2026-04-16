package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 接口调用记录实体类
 * <p>
 * 对应数据库表：fx_api_call_log_YYYYMM（按月分表）
 * 用于存储接口调用的详细记录，包括请求参数、响应数据、调用耗时等
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_call_log")
public class ApiCallLog extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接口配置表 ID（关联 fx_api_config 表）
     */
    private Long apiConfigId;

    /**
     * 调用方向：INBOUND-外对内，OUTBOUND-内调外
     */
    private String callDirection;

    /**
     * 调用方 IP 地址
     */
    private String callerIp;

    /**
     * 请求参数（JSON 格式）
     */
    private String requestData;

    /**
     * 响应数据（JSON 格式）
     */
    private String responseData;

    /**
     * 调用状态：SUCCESS-成功，FAIL-失败
     */
    private String callStatus;

    /**
     * 错误信息（调用失败时记录）
     */
    private String errorMessage;

    /**
     * 调用耗时（毫秒）
     */
    private Integer costTimeMs;

    /**
     * 调用时间
     */
    private LocalDateTime callTime;
}
