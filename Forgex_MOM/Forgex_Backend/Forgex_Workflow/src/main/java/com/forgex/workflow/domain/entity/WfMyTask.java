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
package com.forgex.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我收到的审批实体。
 * <p>
 * 映射表：{@code wf_my_task}。用于快速查询当前用户待处理的审批任务。
 * </p>
 *
 * <p>任务状态：</p>
 * <ul>
 *   <li>0=待处理</li>
 *   <li>1=已处理</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
@TableName("wf_my_task")
public class WfMyTask {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批任务执行表ID
     */
    private Long executionId;

    /**
     * 审批任务名称
     */
    private String taskName;

    /**
     * 当前节点ID
     */
    private Long nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 审批人ID集合（JSON数组）
     * <p>
     * 格式：[10001, 10002, 10003]
     * </p>
     */
    private String approverIds;

    /**
     * 状态
     * <p>
     * 0=待处理<br>
     * 1=已处理
     * </p>
     */
    private Integer status;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private LocalDateTime createTime;
}