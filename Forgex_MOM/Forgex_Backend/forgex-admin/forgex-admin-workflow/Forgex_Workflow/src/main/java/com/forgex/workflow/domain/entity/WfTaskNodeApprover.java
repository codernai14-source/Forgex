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
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批任务节点审批人配置实体。
 * <p>
 * 映射表：{@code wf_task_node_approver}。用于配置审批节点的审批人。
 * </p>
 *
 * <p>审批人类型：</p>
 * <ul>
 *   <li>1=单人：指定具体用户</li>
 *   <li>2=部门：部门内所有用户</li>
 *   <li>3=角色：角色下所有用户</li>
 *   <li>4=职位：职位下所有用户</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
@TableName("wf_task_node_approver")
public class WfTaskNodeApprover {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批任务节点配置表ID
     */
    private Long nodeConfigId;

    /**
     * 审批人类型
     * <p>
     * 1=单人<br>
     * 2=部门<br>
     * 3=角色<br>
     * 4=职位
     * </p>
     */
    private Integer approverType;

    /**
     * 具体ID集合（JSON数组）
     * <p>
     * 格式：[1, 2, 3]
     * </p>
     */
    private String approverIds;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0=未删除，1=已删除
     */
    @TableField("deleted")
    private Integer deleted;
}