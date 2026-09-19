package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class WfTaskNodeRuleDTO {

    private Long id;

    private String ruleName;

    private Integer ruleType;

    private Integer approveMode;

    private BigDecimal approvalThreshold;

    private Integer sortOrder;

    private Integer timeoutHours;

    private Integer timeoutAction;

    private Boolean allowInitiatorSelect;

    private Integer superiorLevel;

    private Boolean allowAddSign;

    private Boolean allowTransfer;

    private Boolean allowDelegate;

    private Boolean allowRecall;

    private List<Long> fallbackApproverIds = new ArrayList<>();

    private List<WfNodeApproverDTO> approvers = new ArrayList<>();

    private String extraConfig;
}
