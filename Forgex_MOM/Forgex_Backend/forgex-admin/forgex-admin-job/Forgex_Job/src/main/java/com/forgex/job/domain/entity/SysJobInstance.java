package com.forgex.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 执行器实例实体。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_instance")
public class SysJobInstance extends BaseEntity {
    private String instanceId;
    private String serviceName;
    private String ip;
    private Integer port;
    private String pid;
    private Integer status;
    private Integer runningCount;
    private LocalDateTime lastHeartbeatTime;
    private LocalDateTime startTime;
    private Integer maintenance;
}
