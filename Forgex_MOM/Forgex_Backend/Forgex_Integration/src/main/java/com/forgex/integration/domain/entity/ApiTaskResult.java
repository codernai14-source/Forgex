package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_task_result")
public class ApiTaskResult extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String taskId;

    private String traceId;

    private Long apiConfigId;

    private String apiCode;

    private String direction;

    private String status;

    private String resultType;

    private String resultData;

    private String errorMessage;

    private Integer costTimeMs;

    private LocalDateTime finishedTime;

    private LocalDateTime expireTime;
}
