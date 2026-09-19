package com.forgex.workflow.domain.dto;

import lombok.Data;

/**
 * wf任务草稿编辑器数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class WfTaskDraftEditorDTO {

    /**
     * 草稿 ID。
     */
    private Long draftId;

    /**
     * published ID。
     */
    private Long publishedId;

    /**
     * 任务编码。
     */
    private String taskCode;

    /**
     * 分类编码。
     */
    private String categoryCode;

    /**
     * 任务名称。
     */
    private String taskName;

    /**
     * 任务名称国际化json。
     */
    private String taskNameI18nJson;

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
     * 表单内容。
     */
    private String formContent;

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
     * 是否已published。
     */
    private Boolean hasPublished;

    /**
     * 配置stage。
     */
    private String configStage;
}
