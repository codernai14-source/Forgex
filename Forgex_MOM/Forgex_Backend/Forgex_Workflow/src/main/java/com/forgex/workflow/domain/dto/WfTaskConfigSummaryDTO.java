package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WfTaskConfigSummaryDTO {

    private Long id;

    private Long publishedId;

    private Long draftId;

    private String taskName;

    private String taskCode;

    private String categoryCode;

    private String interpreterBean;

    private Integer formType;

    private String formPath;

    private Integer status;

    private String remark;

    private Integer publishedVersion;

    private Integer draftVersion;

    private Boolean hasDraft;

    private String displayStage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
