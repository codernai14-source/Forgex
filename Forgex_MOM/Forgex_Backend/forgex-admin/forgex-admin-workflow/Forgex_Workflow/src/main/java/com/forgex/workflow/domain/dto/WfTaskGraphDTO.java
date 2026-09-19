package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WfTaskGraphDTO {

    private Long draftId;

    private String taskCode;

    private List<WfTaskNodeEditorDTO> nodes = new ArrayList<>();

    private List<WfTaskEdgeDTO> edges = new ArrayList<>();

    private List<WfLowCodeFieldMetaDTO> availableFormFields = new ArrayList<>();
}
