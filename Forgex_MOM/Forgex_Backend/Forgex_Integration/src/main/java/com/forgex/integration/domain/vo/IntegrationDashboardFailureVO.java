package com.forgex.integration.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页最近失败调用项。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class IntegrationDashboardFailureVO {

    /**
     * 调用日志 ID。
     */
    private Long id;

    /**
     * 接口配置 ID。
     */
    private Long apiConfigId;

    /**
     * 接口编码。
     */
    private String apiCode;

    /**
     * 接口名称。
     */
    private String apiName;

    /**
     * 调用方向，INBOUND-外调内，OUTBOUND-内调外。
     */
    private String callDirection;

    /**
     * 调用状态。
     */
    private String callStatus;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 调用方 IP。
     */
    private String callerIp;

    /**
     * 调用时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime callTime;

    /**
     * 耗时，单位毫秒。
     */
    private Integer costTimeMs;
}
