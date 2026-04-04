package com.forgex.workflow.domain.dto;

import lombok.Data;

@Data
public class WfTaskEdgeDTO {

    private String id;

    private String sourceNodeKey;

    private String targetNodeKey;
}
