package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * wf任务配置summary数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class WfTaskConfigSummaryDTO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * published ID。
     */
    private Long publishedId;

    /**
     * 草稿 ID。
     */
    private Long draftId;

    /**
     * 任务名称。
     */
    private String taskName;

    /**
     * 任务编码。
     */
    private String taskCode;

    /**
     * 分类编码。
     */
    private String categoryCode;

    /**
     * interpreterbean。
     */
    private String interpreterBean;

    /**
     * 表单类型。
     */
    private Integer formType;

    /**
     * 表单路径。
     */
    private String formPath;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;

    /**
     * publishedversion。
     */
    private Integer publishedVersion;

    /**
     * 草稿version。
     */
    private Integer draftVersion;

    /**
     * 是否已草稿。
     */
    private Boolean hasDraft;

    /**
     * displaystage。
     */
    private String displayStage;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
