package com.forgex.workflow.domain.dto;

import lombok.Data;

@Data
public class WfTaskDraftEditorDTO {

    private Long draftId;

    private Long publishedId;

    private String taskCode;

    private String taskName;

    private String taskNameI18nJson;

    private String interpreterBean;

    private Integer formType;

    private String formPath;

    private String formContent;

    private Integer status;

    private String remark;

    private Integer publishedVersion;

    private Integer draftVersion;

    private Boolean hasPublished;

    private String configStage;
}
