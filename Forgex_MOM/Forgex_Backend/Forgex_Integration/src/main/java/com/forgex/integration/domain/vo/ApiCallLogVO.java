package com.forgex.integration.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口调用记录视图对象
 * <p>
 * 用于向前端展示调用记录信息，包含格式化的时间等字段
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see com.forgex.integration.domain.dto.ApiCallLogDTO
 */
@Data
public class ApiCallLogVO {

    /**
     * 主键 ID（雪花算法生成）
     */
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime callTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
