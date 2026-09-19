package com.forgex.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批节点抄送配置实体。
 * <p>
 * 映射表：{@code wf_task_node_cc}。审批节点在仍需人批的前提下，
 * 额外配置用户 / 部门 / 角色 / 岗位作为抄送对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-09-11
 * @see WfTaskNodeApprover
 * @see WfTaskNodeConfig
 */
@Data
@TableName("wf_task_node_cc")
public class WfTaskNodeCc {

    /**
     * 主键。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批任务节点配置表 ID。
     */
    private Long nodeConfigId;

    /**
     * 抄送对象类型。
     * <p>1=用户，2=部门，3=角色，4=岗位。</p>
     */
    private Integer ccType;

    /**
     * 抄送对象 ID 集合（JSON 字符串数组）。
     */
    private String ccIds;

    /**
     * 租户 ID。
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 创建时间。
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0=未删除，1=已删除。
     */
    @TableField("deleted")
    private Integer deleted;
}
