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
 * 审批任务节点配置实体。
 * <p>
 * 映射表：{@code wf_task_node_config}。用于配置审批任务的节点信息。
 * </p>
 *
 * <p>节点类型：</p>
 * <ul>
 *   <li>1=开始节点：任务开始节点，每个任务只有一个</li>
 *   <li>2=结束节点：任务结束节点，每个任务只有一个</li>
 *   <li>3=审核节点：需要审批的节点</li>
 *   <li>4=普通节点：不需要审批的节点</li>
 *   <li>5=分支节点：条件分支节点</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Data
@TableName("wf_task_node_config")
public class WfTaskNodeConfig {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批任务配置表ID
     */
    private Long taskConfigId;

    /**
     * 节点类型
     * <p>
     * 1=开始节点<br>
     * 2=结束节点<br>
     * 3=审核节点<br>
     * 4=普通节点<br>
     * 5=分支节点
     * </p>
     */
    private Integer nodeType;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点名称多语言JSON
     * <p>
     * 格式：{"zh-CN": "中文", "en-US": "English", ...}
     * </p>
     */
    private String nodeNameI18nJson;

    /**
     * 节点层级
     * <p>
     * 用于确定节点在流程中的位置
     * </p>
     */
    private Integer nodeLevel;

    /**
     * 前置层级
     */
    private Integer preLevel;

    /**
     * 前置节点ID集合（JSON数组）
     * <p>
     * 格式：[1, 2, 3]
     * </p>
     */
    private String preNodeIds;

    /**
     * 后置层级
     */
    private Integer nextLevel;

    /**
     * 后置节点ID集合（JSON数组）
     * <p>
     * 格式：[4, 5, 6]
     * </p>
     */
    private String nextNodeIds;

    /**
     * 审核类型
     * <p>
     * 1=会签（所有审批人都同意才通过）<br>
     * 2=或签（任一审批人同意即通过）<br>
     * 3=抄送（仅抄送，不需要审批）<br>
     * 4=会签投票（超过半数同意即通过）<br>
     * 5=逐个审批（按顺序逐个审批）
     * </p>
     */
    private Integer approveType;

    /**
     * 分支条件设置（JSON）
     * <p>
     * 格式：
     * {
     *   "conditions": [
     *     {"field": "amount", "operator": ">=", "value": "10000", "nextNodeId": 123},
     *     {"field": "amount", "operator": "<", "value": "10000", "nextNodeId": 124}
     *   ],
     *   "defaultNodeId": 125
     * }
     * </p>
     */
    private String branchConditions;

    /**
     * 排序号
     */
    private Integer orderNum;

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