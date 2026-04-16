package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WfTaskNodeEditorDTO {

    private String nodeKey;

    private Integer nodeType;

    private String nodeName;

    private Integer approveType;

    private Double canvasX;

    private Double canvasY;

    private String defaultBranchNodeKey;

    private List<WfNodeApproverDTO> approvers = new ArrayList<>();

    private List<WfTaskNodeRuleDTO> ruleConfigs = new ArrayList<>();

    private List<WfBranchRuleDTO> branchRules = new ArrayList<>();
}
