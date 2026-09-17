package com.forgex.workflow.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程设计器节点编辑对象。
 * <p>
 * 用于草稿图保存 / 回填。审批节点除审批人外，可附加抄送对象，
 * 抄送不能替代审批人。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @see WfNodeApproverDTO
 * @see com.forgex.workflow.service.impl.WfTaskConfigServiceImpl#persistGraph
 */
@Data
public class WfTaskNodeEditorDTO {

    /**
     * 设计器节点键。
     */
    private String nodeKey;

    /**
     * 节点类型。
     */
    private Integer nodeType;

    /**
     * 节点名称。
     */
    private String nodeName;

    /**
     * 审批类型。
     */
    private Integer approveType;

    /**
     * 画布 X 坐标。
     */
    private Double canvasX;

    /**
     * 画布 Y 坐标。
     */
    private Double canvasY;

    /**
     * 默认分支目标节点键。
     */
    private String defaultBranchNodeKey;

    /**
     * 是否启用节点抄送。
     */
    private Boolean ccEnabled = Boolean.FALSE;

    /**
     * 抄送对象列表，复用审批人来源结构（类型 + ID 集合）。
     */
    private List<WfNodeApproverDTO> ccTargets = new ArrayList<>();

    /**
     * 审批人来源。
     */
    private List<WfNodeApproverDTO> approvers = new ArrayList<>();

    /**
     * 节点规则配置。
     */
    private List<WfTaskNodeRuleDTO> ruleConfigs = new ArrayList<>();

    /**
     * 分支规则。
     */
    private List<WfBranchRuleDTO> branchRules = new ArrayList<>();
}
