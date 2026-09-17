package com.forgex.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批节点抄送运行时记录实体。
 * <p>
 * 映射表：{@code wf_task_cc_record}。节点进入并激活时落一条用户快照，
 * 不写入 {@code wf_my_task}。同一 {@code execution_detail_id + cc_user_id} 只保留一行。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-09-11
 * @see WfTaskNodeCc
 * @see WfTaskExecutionDetail
 */
@Data
@TableName("wf_task_cc_record")
public class WfTaskCcRecord {

    /**
     * 主键。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批执行单 ID。
     */
    private Long executionId;

    /**
     * 本轮节点进入对应的执行明细 ID。
     */
    private Long executionDetailId;

    /**
     * 节点配置 ID。
     */
    private Long nodeId;

    /**
     * 抄送节点名称快照。
     */
    private String nodeName;

    /**
     * 被抄送用户 ID。
     */
    private Long ccUserId;

    /**
     * 被抄送用户名称快照。
     */
    private String ccUserName;

    /**
     * 来源类型：1=用户，2=部门，3=角色，4=岗位。
     */
    private Integer ccSourceType;

    /**
     * 解析来源快照 JSON。
     */
    private String sourceSnapshot;

    /**
     * 已读状态：0=未读，1=已读。
     */
    private Integer readStatus;

    /**
     * 已读时间。
     */
    private LocalDateTime readTime;

    /**
     * 通知状态：0=未发送，1=成功，2=失败。
     */
    private Integer notifyStatus;

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
     * 更新时间。
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0=未删除，1=已删除。
     */
    @TableField("deleted")
    private Integer deleted;
}
