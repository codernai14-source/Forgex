package com.forgex.workflow.domain.param;

import com.forgex.workflow.domain.dto.WfTaskEdgeDTO;
import com.forgex.workflow.domain.dto.WfTaskNodeEditorDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WfTaskGraphSaveParam {

    @NotNull(message = "草稿ID不能为空")
    private Long draftId;

    private List<WfTaskNodeEditorDTO> nodes = new ArrayList<>();

    private List<WfTaskEdgeDTO> edges = new ArrayList<>();
}
