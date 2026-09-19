package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批执行单抄送人展示对象。
 * <p>
 * 供轨迹面板按执行单列出本单抄送人，不承担列表分页职责。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-09-11
 * @see WfExecutionDTO
 */
@Data
public class WfCcRecordDTO {

    /**
     * 抄送记录 ID。
     */
    private Long id;

    /**
     * 执行单 ID。
     */
    private Long executionId;

    /**
     * 节点配置 ID。
     */
    private Long nodeId;

    /**
     * 抄送节点名称。
     */
    private String nodeName;

    /**
     * 被抄送用户 ID。
     */
    private Long ccUserId;

    /**
     * 被抄送用户名称。
     */
    private String ccUserName;

    /**
     * 已读状态：0=未读，1=已读。
     */
    private Integer readStatus;

    /**
     * 抄送时间。
     */
    private LocalDateTime createTime;
}
