package com.forgex.workflow.domain.dto;

import lombok.Data;

@Data
public class WfBranchRuleDTO {

    private String fieldKey;

    private String fieldLabel;

    private String operator;

    private String value;

    private String nextNodeKey;
}
