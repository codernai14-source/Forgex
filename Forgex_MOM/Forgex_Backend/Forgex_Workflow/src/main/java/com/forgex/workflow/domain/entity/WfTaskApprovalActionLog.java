package com.forgex.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批动作日志实体。
 * <p>
 * 对应表 {@code wf_task_approval_action_log}，用于记录审批过程中的所有操作日志，
 * 包括审批、转交、委托、加签、超时处理等操作。
 * </p>
 *
 * <p>动作类型说明：</p>
 * <ul>
 *   <li>1=审批通过</li>
 *   <li>2=审批驳回</li>
 *   <li>3=转交</li>
 *   <li>4=委托</li>
 *   <li>5=加签</li>
 *   <li>6=超时处理</li>
 *   <li>7=撤回</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 * @see WfTaskExecution 审批任务执行实体
 * @see WfTaskApprovalInstance 审批实例实体
 */
@Data
@TableName("wf_task_approval_action_log")
public class WfTaskApprovalActionLog {

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
     * 审批实例 ID。
     * <p>
     * 关联 {@code wf_task_approval_instance} 表的主键。
     * </p>
     *
     * @see WfTaskApprovalInstance#id
     */
    private Long approvalInstanceId;

    /**
     * 动作类型。
     * <p>
     * 1=审批通过<br>
     * 2=审批驳回<br>
     * 3=转交<br>
     * 4=委托<br>
     * 5=加签<br>
     * 6=超时处理<br>
     * 7=撤回
     * </p>
     */
    private Integer actionType;

    /**
     * 操作人 ID。
     * <p>
     * 关联系统用户表的主键。
     * </p>
     */
    private Long operatorId;

    /**
     * 操作人名称。
     */
    private String operatorName;

    /**
     * 目标用户 ID。
     * <p>
     * 转交、委托、加签等场景下的目标用户 ID。
     * 关联系统用户表的主键。
     * </p>
     */
    private Long targetUserId;

    /**
     * 目标用户名称。
     */
    private String targetUserName;

    /**
     * 操作意见。
     * <p>
     * 审批人填写的审批意见或备注。
     * </p>
     */
    private String actionComment;

    /**
     * 操作快照（JSON）。
     * <p>
     * 记录操作时的完整上下文信息，包括操作前后的状态变化、
     * 转交/委托/加签的详细信息等。
     * 示例：
     * {
     *   "beforeStatus": 1,
     *   "afterStatus": 2,
     *   "actionDetail": {"type": "transfer", "reason": "出差"}
     * }
     * </p>
     */
    private String actionSnapshot;

    /**
     * 租户 ID。
     */
    private Long tenantId;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0=未删除，1=已删除。
     */
    @TableField("deleted")
    private Integer deleted;
}
