/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户初始化任务实体
 * <p>
 * 记录租户初始化的进度和状态，用于前端实时展示创建进度。
 * </p>
 * <p>
 * 主要字段：
 * <ul>
 *   <li>tenantId - 租户 ID</li>
 *   <li>tenantName - 租户名称</li>
 *   <li>tenantType - 租户类型</li>
 *   <li>status - 状态：PENDING-等待中，RUNNING-进行中，SUCCESS-成功，FAILED-失败</li>
 *   <li>progress - 进度百分比（0-100）</li>
 *   <li>currentStep - 当前步骤描述</li>
 *   <li>errorMessage - 错误信息</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_tenant_init_task")
public class SysTenantInitTask {
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 租户 ID
     */
    private Long tenantId;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 租户类型
     */
    private String tenantType;
    
    /**
     * 状态
     * <p>PENDING-等待中，RUNNING-进行中，SUCCESS-成功，FAILED-失败</p>
     */
    private String status;
    
    /**
     * 进度百分比（0-100）
     */
    private Integer progress;
    
    /**
     * 当前步骤描述
     */
    private String currentStep;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
