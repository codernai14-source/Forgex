package com.forgex.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批实例实体。
 * <p>
 * 对应表 {@code wf_task_approval_instance}，用于记录审批流程中每个审批人的审批实例信息。
 * 一个审批节点可能有多个审批人，每个审批人对应一个审批实例。
 * </p>
 *
 * <p>审批人来源类型说明：</p>
 * <ul>
 *   <li>1=指定审批人：直接指定的具体用户</li>
 *   <li>2=部门审批人：部门内的所有用户</li>
 *   <li>3=角色审批人：角色下的所有用户</li>
 *   <li>4=职位审批人：职位下的所有用户</li>
 *   <li>5=发起人自选：由发起人自行选择审批人</li>
 *   <li>6=连续多级审批：上级审批</li>
 * </ul>
 *
 * <p>状态说明：</p>
 * <ul>
 *   <li>0=待审批</li>
 *   <li>1=已审批</li>
 *   <li>2=已驳回</li>
 * </ul>
 *
 * <p>动作类型说明：</p>
 * <ul>
 *   <li>1=审批通过</li>
 *   <li>2=审批驳回</li>
 *   <li>3=转交</li>
 *   <li>4=委托</li>
 *   <li>5=加签</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 * @see WfTaskExecution 审批任务执行实体
 * @see WfTaskApprovalActionLog 审批动作日志实体
 */
@Data
@TableName("wf_task_approval_instance")
public class WfTaskApprovalInstance {

    /**
     * 主键 ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批任务执行表 ID。
     * <p>
     * 关联 {@code wf_task_execution} 表的主键。
     * </p>
     *
     * @see WfTaskExecution#id
     */
    private Long executionId;

    /**
     * 审批任务执行详情表 ID。
     * <p>
     * 关联 {@code wf_task_execution_detail} 表的主键。
     * </p>
     *
     * @see WfTaskExecutionDetail#id
     */
    private Long executionDetailId;

    /**
     * 审批节点 ID。
     * <p>
     * 关联 {@code wf_task_node_config} 表的主键。
     * </p>
     *
     * @see WfTaskNodeConfig#id
     */
    private Long nodeId;

    /**
     * 实例编号。
     * <p>
     * 用于标识审批实例的唯一编号，可用于消息通知等场景。
     * </p>
     */
    private String instanceNo;

    /**
     * 审批人 ID。
     * <p>
     * 关联系统用户表的主键。
     * </p>
     */
    private Long approverId;

    /**
     * 审批人名称。
     */
    private String approverName;

    /**
     * 审批人来源类型。
     * <p>
     * 1=指定审批人<br>
     * 2=部门审批人<br>
     * 3=角色审批人<br>
     * 4=职位审批人<br>
     * 5=发起人自选<br>
     * 6=连续多级审批
     * </p>
     */
    private Integer approverSourceType;

    /**
     * 来源规则 ID。
     * <p>
     * 关联 {@code wf_task_node_rule} 表的主键，
     * 用于追溯审批人的来源规则配置。
     * </p>
     *
     * @see WfTaskNodeRule#id
     */
    private Long sourceRuleId;

    /**
     * 来源快照（JSON）。
     * <p>
     * 记录审批人生成时的规则快照，包括审批人类型、
     * 审批模式、阈值等配置信息，用于后续追溯。
     * 示例：
     * {
     *   "ruleType": 1,
     *   "approveMode": 1,
     *   "approverIds": [10001, 10002]
     * }
     * </p>
     */
    private String sourceSnapshot;

    /**
     * 审批状态。
     * <p>
     * 0=待审批<br>
     * 1=已审批<br>
     * 2=已驳回
     * </p>
     */
    private Integer status;

    /**
     * 动作类型。
     * <p>
     * 1=审批通过<br>
     * 2=审批驳回<br>
     * 3=转交<br>
     * 4=委托<br>
     * 5=加签
     * </p>
     */
    private Integer actionType;

    /**
     * 审批意见。
     * <p>
     * 审批人填写的审批意见或备注。
     * </p>
     */
    private String comment;

    /**
     * 审批时间。
     */
    private LocalDateTime approveTime;

    /**
     * 审批截止时间。
     */
    private LocalDateTime deadlineTime;

    /**
     * 是否已激活。
     * <p>
     * true=已激活，false=未激活。
     * 用于会签、或签等场景，标识该审批实例是否已被激活处理。
     * </p>
     */
    private Boolean activated;

    /**
     * 委托人 ID。
     * <p>
     * 如果是委托场景，记录原始委托人 ID。
     * 关联系统用户表的主键。
     * </p>
     */
    private Long delegateFromUserId;

    /**
     * 转交人 ID。
     * <p>
     * 如果是转交场景，记录原始转交人 ID。
     * 关联系统用户表的主键。
     * </p>
     */
    private Long transferFromUserId;

    /**
     * 租户 ID。
     */
    private Long tenantId;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0=未删除，1=已删除。
     */
    @TableField("deleted")
    private Integer deleted;
}
