package com.forgex.integration.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步任务结果返回
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@Builder
public class ApiTaskResultVO {

    /**
     * 任务 ID。
     */
    private String taskId;

    /**
     * 状态。
     */
    private String status;

    /**
     * 结果类型。
     */
    private String resultType;

    /**
     * 结果数据。
     */
    private String resultData;

    /**
     * 错误消息。
     */
    private String errorMessage;

    /**
     * 完成时间。
     */
    private LocalDateTime finishedTime;
}
