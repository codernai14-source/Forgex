package com.forgex.workflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.workflow.domain.dto.WfBranchRuleDTO;
import com.forgex.workflow.domain.dto.WfNodeApproverDTO;
import com.forgex.workflow.domain.dto.WfTaskConfigDTO;
import com.forgex.workflow.domain.dto.WfTaskConfigSummaryDTO;
import com.forgex.workflow.domain.dto.WfTaskDraftEditorDTO;
import com.forgex.workflow.domain.dto.WfTaskEdgeDTO;
import com.forgex.workflow.domain.dto.WfTaskGraphDTO;
import com.forgex.workflow.domain.dto.WfTaskNodeEditorDTO;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskNodeApprover;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import com.forgex.workflow.domain.param.WfTaskConfigQueryParam;
import com.forgex.workflow.domain.param.WfTaskConfigSaveParam;
import com.forgex.workflow.domain.param.WfTaskDraftEditorQueryParam;
import com.forgex.workflow.domain.param.WfTaskGraphSaveParam;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskNodeApproverMapper;
import com.forgex.workflow.mapper.WfTaskNodeConfigMapper;
import com.forgex.workflow.service.IWfTaskConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@DS("workflow")
@RequiredArgsConstructor
public class WfTaskConfigServiceImpl implements IWfTaskConfigService {

    private static final List<String> VISIBLE_STAGES = Arrays.asList(
            WorkflowConstants.ConfigStage.DRAFT,
            WorkflowConstants.ConfigStage.PUBLISHED
    );

    private final WfTaskConfigMapper taskConfigMapper;
    private final WfTaskNodeConfigMapper nodeConfigMapper;
    private final WfTaskNodeApproverMapper nodeApproverMapper;

    @Override
    public Page<WfTaskConfigSummaryDTO> page(WfTaskConfigQueryParam param) {
        Long tenantId = requireCurrentTenantId();
        LambdaQueryWrapper<WfTaskConfig> wrapper = new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getDeleted, false)
                .in(WfTaskConfig::getConfigStage, VISIBLE_STAGES)
                .orderByDesc(WfTaskConfig::getUpdateTime)
                .orderByDesc(WfTaskConfig::getCreateTime);
        if (StringUtils.hasText(param.getTaskName())) {
            wrapper.like(WfTaskConfig::getTaskName, param.getTaskName().trim());
        }
        if (StringUtils.hasText(param.getTaskCode())) {
            wrapper.like(WfTaskConfig::getTaskCode, param.getTaskCode().trim());
        }
        if (param.getStatus() != null) {
            wrapper.eq(WfTaskConfig::getStatus, param.getStatus());
        }
        List<WfTaskConfig> visibleConfigs = taskConfigMapper.selectList(wrapper);
        Map<String, List<WfTaskConfig>> grouped = visibleConfigs.stream()
                .collect(Collectors.groupingBy(WfTaskConfig::getTaskCode, LinkedHashMap::new, Collectors.toList()));
        List<WfTaskConfigSummaryDTO> summaries = grouped.values().stream()
                .map(this::buildSummary)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WfTaskConfigSummaryDTO::getUpdateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        long current = normalizePageNum(param.getPageNum());
        long size = normalizePageSize(param.getPageSize());
        int fromIndex = (int) Math.min((current - 1) * size, summaries.size());
        int toIndex = (int) Math.min(fromIndex + size, summaries.size());
        Page<WfTaskConfigSummaryDTO> page = new Page<>();
        page.setCurrent(current);
        page.setSize(size);
        page.setTotal(summaries.size());
        page.setRecords(new ArrayList<>(summaries.subList(fromIndex, toIndex)));
        return page;
    }

    @Override
    public List<WfTaskConfigDTO> list(WfTaskConfigQueryParam param) {
        Long tenantId = requireCurrentTenantId();
        LambdaQueryWrapper<WfTaskConfig> wrapper = new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getDeleted, false)
                .eq(WfTaskConfig::getConfigStage, WorkflowConstants.ConfigStage.PUBLISHED)
                .orderByDesc(WfTaskConfig::getVersion)
                .orderByDesc(WfTaskConfig::getUpdateTime);
        if (StringUtils.hasText(param.getTaskName())) {
            wrapper.like(WfTaskConfig::getTaskName, param.getTaskName().trim());
        }
        if (StringUtils.hasText(param.getTaskCode())) {
            wrapper.like(WfTaskConfig::getTaskCode, param.getTaskCode().trim());
        }
        if (param.getStatus() != null) {
            wrapper.eq(WfTaskConfig::getStatus, param.getStatus());
        }
        return taskConfigMapper.selectList(wrapper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WfTaskConfigDTO getById(Long id) {
        return convertToDTO(requireTaskConfig(id, requireCurrentTenantId()));
    }

    @Override
    public WfTaskConfigDTO getByCode(String taskCode) {
        WfTaskConfig published = findPublishedByTaskCode(taskCode, requireCurrentTenantId());
        return published == null ? null : convertToDTO(published);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WfTaskConfigSaveParam param) {
        return saveDraftBaseInfo(param).getDraftId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(WfTaskConfigSaveParam param) {
        saveDraftBaseInfo(param);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        Long tenantId = requireCurrentTenantId();
        WfTaskConfig config = requireTaskConfig(id, tenantId);
        String taskCode = config.getTaskCode();

        WfTaskConfig draft = findDraftByTaskCode(taskCode, tenantId);
        if (draft != null) {
            draft.setDeleted(true);
            taskConfigMapper.updateById(draft);
            markTaskNodesDeleted(draft.getId(), tenantId);
        }

        WfTaskConfig published = findPublishedByTaskCode(taskCode, tenantId);
        if (published != null) {
            published.setConfigStage(WorkflowConstants.ConfigStage.ARCHIVED);
            taskConfigMapper.updateById(published);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        WfTaskConfig draft = requireDraftById(id, requireCurrentTenantId());
        draft.setStatus(status);
        taskConfigMapper.updateById(draft);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfTaskDraftEditorDTO getOrCreateDraftEditor(WfTaskDraftEditorQueryParam param) {
        Long tenantId = requireCurrentTenantId();
        WfTaskConfig draft = resolveOrCreateDraft(param, tenantId);
        return buildDraftEditor(draft, findPublishedByTaskCode(draft.getTaskCode(), tenantId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfTaskDraftEditorDTO saveDraftBaseInfo(WfTaskConfigSaveParam param) {
        Long tenantId = requireCurrentTenantId();
        String targetTaskCode = trimToNull(param.getTaskCode());
        if (!StringUtils.hasText(targetTaskCode)) {
            throw new BusinessException("任务编码不能为空");
        }

        if (param.getId() == null) {
            if (existsVisibleTaskCode(targetTaskCode, tenantId)) {
                throw new BusinessException("任务编码已存在");
            }
            WfTaskConfig draft = new WfTaskConfig();
            applyBaseInfo(param, draft);
            draft.setTenantId(tenantId);
            draft.setVersion(nextVersion(targetTaskCode, tenantId));
            draft.setStatus(draft.getStatus() == null ? 1 : draft.getStatus());
            draft.setConfigStage(WorkflowConstants.ConfigStage.DRAFT);
            taskConfigMapper.insert(draft);
            return buildDraftEditor(draft, findPublishedByTaskCode(targetTaskCode, tenantId));
        }

        WfTaskConfig draft = requireDraftById(param.getId(), tenantId);
        String originalTaskCode = draft.getTaskCode();
        if (!Objects.equals(originalTaskCode, targetTaskCode)) {
            if (findPublishedByTaskCode(originalTaskCode, tenantId) != null) {
                throw new BusinessException("已发布流程不允许修改任务编码");
            }
            if (existsVisibleTaskCode(targetTaskCode, tenantId)) {
                throw new BusinessException("任务编码已存在");
            }
        }

        applyBaseInfo(param, draft);
        if (!Objects.equals(originalTaskCode, targetTaskCode)) {
            draft.setVersion(nextVersion(targetTaskCode, tenantId));
        }
        draft.setConfigStage(WorkflowConstants.ConfigStage.DRAFT);
        taskConfigMapper.updateById(draft);
        return buildDraftEditor(draft, findPublishedByTaskCode(draft.getTaskCode(), tenantId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WfTaskGraphDTO getDraftGraph(WfTaskDraftEditorQueryParam param) {
        Long tenantId = requireCurrentTenantId();
        return buildGraph(resolveOrCreateDraft(param, tenantId), tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveDraftGraph(WfTaskGraphSaveParam param) {
        Long tenantId = requireCurrentTenantId();
        WfTaskConfig draft = requireDraftById(param.getDraftId(), tenantId);
        List<WfTaskNodeEditorDTO> nodes = normalizeNodes(param.getNodes());
        List<WfTaskEdgeDTO> edges = normalizeEdges(param.getEdges());
        persistGraph(draft, nodes, edges, tenantId, true);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishDraft(WfTaskDraftEditorQueryParam param) {
        Long tenantId = requireCurrentTenantId();
        WfTaskConfig draft = resolveExistingDraft(param, tenantId);
        validatePersistedGraph(draft, tenantId);

        WfTaskConfig published = findPublishedByTaskCode(draft.getTaskCode(), tenantId);
        if (published != null) {
            published.setConfigStage(WorkflowConstants.ConfigStage.ARCHIVED);
            taskConfigMapper.updateById(published);
        }

        draft.setConfigStage(WorkflowConstants.ConfigStage.PUBLISHED);
        taskConfigMapper.updateById(draft);
        return true;
    }

    private long normalizePageNum(Integer pageNum) { return pageNum == null || pageNum < 1 ? 1L : pageNum.longValue(); }
    private long normalizePageSize(Integer pageSize) { return pageSize == null || pageSize < 1 ? 10L : pageSize.longValue(); }
    private void applyBaseInfo(WfTaskConfigSaveParam param, WfTaskConfig draft) {
        draft.setTaskName(trimToNull(param.getTaskName()));
        draft.setTaskNameI18nJson(trimToNull(param.getTaskNameI18nJson()));
        draft.setTaskCode(trimToNull(param.getTaskCode()));
        draft.setInterpreterBean(trimToNull(param.getInterpreterBean()));
        draft.setFormType(param.getFormType());
        draft.setFormPath(trimToNull(param.getFormPath()));
        draft.setFormContent(trimToNull(param.getFormContent()));
        draft.setStatus(param.getStatus());
        draft.setRemark(trimToNull(param.getRemark()));
    }
    private WfTaskConfigSummaryDTO buildSummary(List<WfTaskConfig> configs) {
        WfTaskConfig published = pickStage(configs, WorkflowConstants.ConfigStage.PUBLISHED);
        WfTaskConfig draft = pickStage(configs, WorkflowConstants.ConfigStage.DRAFT);
        WfTaskConfig display = published != null ? published : draft;
        if (display == null) {
            return null;
        }

        WfTaskConfigSummaryDTO dto = new WfTaskConfigSummaryDTO();
        dto.setId(display.getId());
        dto.setPublishedId(published == null ? null : published.getId());
        dto.setDraftId(draft == null ? null : draft.getId());
        dto.setTaskName(display.getTaskName());
        dto.setTaskCode(display.getTaskCode());
        dto.setInterpreterBean(display.getInterpreterBean());
        dto.setFormType(display.getFormType());
        dto.setFormPath(display.getFormPath());
        dto.setStatus(display.getStatus());
        dto.setRemark(display.getRemark());
        dto.setPublishedVersion(published == null ? null : published.getVersion());
        dto.setDraftVersion(draft == null ? null : draft.getVersion());
        dto.setHasDraft(draft != null);
        dto.setDisplayStage(published != null ? WorkflowConstants.ConfigStage.PUBLISHED : WorkflowConstants.ConfigStage.DRAFT);
        dto.setCreateTime(display.getCreateTime());
        dto.setUpdateTime(resolveLatestUpdateTime(published, draft));
        return dto;
    }
    private LocalDateTime resolveLatestUpdateTime(WfTaskConfig left, WfTaskConfig right) {
        LocalDateTime leftTime = left == null ? null : defaultTime(left.getUpdateTime(), left.getCreateTime());
        LocalDateTime rightTime = right == null ? null : defaultTime(right.getUpdateTime(), right.getCreateTime());
        if (leftTime == null) {
            return rightTime;
        }
        if (rightTime == null) {
            return leftTime;
        }
        return leftTime.isAfter(rightTime) ? leftTime : rightTime;
    }
    private LocalDateTime defaultTime(LocalDateTime first, LocalDateTime second) { return first != null ? first : second; }
    private WfTaskConfig pickStage(List<WfTaskConfig> configs, String stage) {
        return configs.stream()
                .filter(item -> Objects.equals(stage, item.getConfigStage()))
                .max(Comparator.comparing(WfTaskConfig::getVersion, Comparator.nullsLast(Integer::compareTo)))
                .orElse(null);
    }
    private WfTaskDraftEditorDTO buildDraftEditor(WfTaskConfig draft, WfTaskConfig published) {
        if (draft == null) {
            throw new BusinessException("草稿不存在");
        }
        WfTaskDraftEditorDTO dto = new WfTaskDraftEditorDTO();
        dto.setDraftId(draft.getId());
        dto.setPublishedId(published == null ? null : published.getId());
        dto.setTaskCode(draft.getTaskCode());
        dto.setTaskName(draft.getTaskName());
        dto.setTaskNameI18nJson(draft.getTaskNameI18nJson());
        dto.setInterpreterBean(draft.getInterpreterBean());
        dto.setFormType(draft.getFormType());
        dto.setFormPath(draft.getFormPath());
        dto.setFormContent(draft.getFormContent());
        dto.setStatus(draft.getStatus());
        dto.setRemark(draft.getRemark());
        dto.setPublishedVersion(published == null ? null : published.getVersion());
        dto.setDraftVersion(draft.getVersion());
        dto.setHasPublished(published != null);
        dto.setConfigStage(draft.getConfigStage());
        return dto;
    }
    private WfTaskGraphDTO buildGraph(WfTaskConfig taskConfig, Long tenantId) {
        WfTaskGraphDTO dto = new WfTaskGraphDTO();
        dto.setDraftId(taskConfig.getId());
        dto.setTaskCode(taskConfig.getTaskCode());

        List<WfTaskNodeConfig> nodes = listActiveNodes(taskConfig.getId(), tenantId);
        if (nodes.isEmpty()) {
            return dto;
        }

        Map<Long, String> nodeKeyById = new HashMap<>();
        for (WfTaskNodeConfig node : nodes) {
            nodeKeyById.put(node.getId(), resolveNodeKey(node));
        }

        List<Long> nodeIds = nodes.stream().map(WfTaskNodeConfig::getId).collect(Collectors.toList());
        Map<Long, List<WfTaskNodeApprover>> approverMap = listActiveApprovers(nodeIds, tenantId).stream()
                .collect(Collectors.groupingBy(WfTaskNodeApprover::getNodeConfigId, LinkedHashMap::new, Collectors.toList()));

        for (WfTaskNodeConfig node : nodes) {
            WfTaskNodeEditorDTO nodeDto = new WfTaskNodeEditorDTO();
            nodeDto.setNodeKey(resolveNodeKey(node));
            nodeDto.setNodeType(node.getNodeType());
            nodeDto.setNodeName(node.getNodeName());
            nodeDto.setApproveType(node.getApproveType());
            nodeDto.setCanvasX(node.getCanvasX());
            nodeDto.setCanvasY(node.getCanvasY());

            if (Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.APPROVE)) {
                List<WfNodeApproverDTO> approvers = approverMap.getOrDefault(node.getId(), Collections.emptyList()).stream()
                        .map(item -> {
                            WfNodeApproverDTO approverDto = new WfNodeApproverDTO();
                            approverDto.setApproverType(item.getApproverType());
                            approverDto.setApproverIds(parseLongArray(item.getApproverIds()));
                            return approverDto;
                        })
                        .collect(Collectors.toList());
                nodeDto.setApprovers(approvers);
            }

            if (Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.BRANCH)
                    && StringUtils.hasText(node.getBranchConditions())) {
                JSONObject branch = JSON.parseObject(node.getBranchConditions());
                nodeDto.setDefaultBranchNodeKey(resolveBranchNodeKey(branch, "defaultNodeKey",
                        branch.getLong("defaultNodeId"), nodeKeyById));
                JSONArray conditions = branch.getJSONArray("conditions");
                if (conditions != null) {
                    List<WfBranchRuleDTO> rules = new ArrayList<>();
                    for (int i = 0; i < conditions.size(); i++) {
                        JSONObject condition = conditions.getJSONObject(i);
                        if (condition == null) {
                            continue;
                        }
                        WfBranchRuleDTO rule = new WfBranchRuleDTO();
                        rule.setFieldKey(firstText(condition.getString("fieldKey"), condition.getString("field")));
                        rule.setFieldLabel(condition.getString("fieldLabel"));
                        rule.setOperator(condition.getString("operator"));
                        rule.setValue(condition.getString("value"));
                        rule.setNextNodeKey(resolveBranchNodeKey(condition, "nextNodeKey",
                                condition.getLong("nextNodeId"), nodeKeyById));
                        rules.add(rule);
                    }
                    nodeDto.setBranchRules(rules);
                }
            }

            dto.getNodes().add(nodeDto);
        }

        Set<String> edgeKeys = new LinkedHashSet<>();
        for (WfTaskNodeConfig node : nodes) {
            List<Long> nextNodeIds = parseLongArray(node.getNextNodeIds());
            String sourceNodeKey = nodeKeyById.get(node.getId());
            for (Long nextNodeId : nextNodeIds) {
                String targetNodeKey = nodeKeyById.get(nextNodeId);
                if (!StringUtils.hasText(sourceNodeKey) || !StringUtils.hasText(targetNodeKey)) {
                    continue;
                }
                String edgeId = sourceNodeKey + "->" + targetNodeKey;
                if (!edgeKeys.add(edgeId)) {
                    continue;
                }
                WfTaskEdgeDTO edge = new WfTaskEdgeDTO();
                edge.setId(edgeId);
                edge.setSourceNodeKey(sourceNodeKey);
                edge.setTargetNodeKey(targetNodeKey);
                dto.getEdges().add(edge);
            }
        }

        return dto;
    }
    private String resolveBranchNodeKey(JSONObject jsonObject, String keyField, Long fallbackNodeId, Map<Long, String> nodeKeyById) {
        String nodeKey = trimToNull(jsonObject.getString(keyField));
        if (StringUtils.hasText(nodeKey)) {
            return nodeKey;
        }
        return fallbackNodeId == null ? null : nodeKeyById.get(fallbackNodeId);
    }
    private String firstText(String first, String second) { return StringUtils.hasText(first) ? first : second; }
    private void validatePersistedGraph(WfTaskConfig draft, Long tenantId) {
        WfTaskGraphDTO graph = buildGraph(draft, tenantId);
        validateGraph(graph.getNodes(), graph.getEdges());
    }
    private void persistGraph(WfTaskConfig draft, List<WfTaskNodeEditorDTO> nodes, List<WfTaskEdgeDTO> edges,
                              Long tenantId, boolean validateBeforeSave) {
        if (validateBeforeSave) {
            validateGraph(nodes, edges);
        }

        markTaskNodesDeleted(draft.getId(), tenantId);

        Map<String, List<String>> outgoing = buildRelationMap(edges, true);
        Map<String, List<String>> incoming = buildRelationMap(edges, false);
        Map<String, Integer> levels = calculateNodeLevels(nodes, outgoing, incoming);
        Map<String, WfTaskNodeConfig> persistedNodeMap = new LinkedHashMap<>();

        int orderNum = 1;
        for (WfTaskNodeEditorDTO node : nodes) {
            WfTaskNodeConfig entity = new WfTaskNodeConfig();
            entity.setTaskConfigId(draft.getId());
            entity.setTenantId(tenantId);
            entity.setNodeKey(node.getNodeKey());
            entity.setNodeType(node.getNodeType());
            entity.setNodeName(resolveNodeName(node));
            entity.setNodeNameI18nJson(buildNodeI18n(resolveNodeName(node)));
            entity.setNodeLevel(levels.get(node.getNodeKey()));
            entity.setPreLevel(0);
            entity.setPreNodeIds("[]");
            entity.setNextLevel(0);
            entity.setNextNodeIds("[]");
            entity.setApproveType(Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.APPROVE)
                    ? node.getApproveType() : null);
            entity.setBranchConditions(null);
            entity.setCanvasX(node.getCanvasX());
            entity.setCanvasY(node.getCanvasY());
            entity.setOrderNum(orderNum++);
            entity.setDeleted(0);
            nodeConfigMapper.insert(entity);
            persistedNodeMap.put(node.getNodeKey(), entity);
        }

        for (WfTaskNodeEditorDTO node : nodes) {
            List<String> predecessors = incoming.getOrDefault(node.getNodeKey(), Collections.emptyList());
            List<String> successors = outgoing.getOrDefault(node.getNodeKey(), Collections.emptyList());
            WfTaskNodeConfig update = new WfTaskNodeConfig();
            update.setId(persistedNodeMap.get(node.getNodeKey()).getId());
            update.setPreNodeIds(JSON.toJSONString(toNodeIds(predecessors, persistedNodeMap)));
            update.setNextNodeIds(JSON.toJSONString(toNodeIds(successors, persistedNodeMap)));
            update.setPreLevel(resolvePreLevel(predecessors, levels));
            update.setNextLevel(resolveNextLevel(successors, levels));
            if (Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.BRANCH)) {
                update.setBranchConditions(buildBranchConditions(node, persistedNodeMap));
            }
            nodeConfigMapper.updateById(update);
        }

        for (WfTaskNodeEditorDTO node : nodes) {
            if (!Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.APPROVE)) {
                continue;
            }
            WfTaskNodeConfig persistedNode = persistedNodeMap.get(node.getNodeKey());
            for (WfNodeApproverDTO approver : node.getApprovers()) {
                if (approver == null || approver.getApproverType() == null || approver.getApproverIds().isEmpty()) {
                    continue;
                }
                WfTaskNodeApprover entity = new WfTaskNodeApprover();
                entity.setNodeConfigId(persistedNode.getId());
                entity.setApproverType(approver.getApproverType());
                entity.setApproverIds(JSON.toJSONString(approver.getApproverIds()));
                entity.setTenantId(tenantId);
                entity.setDeleted(0);
                nodeApproverMapper.insert(entity);
            }
        }
    }
    private List<WfTaskNodeEditorDTO> normalizeNodes(List<WfTaskNodeEditorDTO> source) {
        List<WfTaskNodeEditorDTO> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (WfTaskNodeEditorDTO item : source) {
            if (item == null) {
                continue;
            }
            item.setNodeKey(trimToNull(item.getNodeKey()));
            item.setNodeName(trimToNull(item.getNodeName()));
            item.setDefaultBranchNodeKey(trimToNull(item.getDefaultBranchNodeKey()));
            item.setCanvasX(item.getCanvasX() == null ? 0D : item.getCanvasX());
            item.setCanvasY(item.getCanvasY() == null ? 0D : item.getCanvasY());
            if (item.getApprovers() == null) {
                item.setApprovers(new ArrayList<>());
            } else {
                List<WfNodeApproverDTO> approvers = new ArrayList<>();
                for (WfNodeApproverDTO approver : item.getApprovers()) {
                    if (approver == null) {
                        continue;
                    }
                    approver.setApproverIds(approver.getApproverIds() == null
                            ? new ArrayList<>() : approver.getApproverIds().stream().filter(Objects::nonNull)
                            .distinct().collect(Collectors.toList()));
                    approvers.add(approver);
                }
                item.setApprovers(approvers);
            }
            if (item.getBranchRules() == null) {
                item.setBranchRules(new ArrayList<>());
            } else {
                List<WfBranchRuleDTO> rules = new ArrayList<>();
                for (WfBranchRuleDTO rule : item.getBranchRules()) {
                    if (rule == null) {
                        continue;
                    }
                    rule.setFieldKey(trimToNull(rule.getFieldKey()));
                    rule.setFieldLabel(trimToNull(rule.getFieldLabel()));
                    rule.setOperator(trimToNull(rule.getOperator()));
                    rule.setValue(trimToNull(rule.getValue()));
                    rule.setNextNodeKey(trimToNull(rule.getNextNodeKey()));
                    rules.add(rule);
                }
                item.setBranchRules(rules);
            }
            result.add(item);
        }
        return result;
    }
    private List<WfTaskEdgeDTO> normalizeEdges(List<WfTaskEdgeDTO> source) {
        List<WfTaskEdgeDTO> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (WfTaskEdgeDTO item : source) {
            if (item == null) {
                continue;
            }
            item.setId(trimToNull(item.getId()));
            item.setSourceNodeKey(trimToNull(item.getSourceNodeKey()));
            item.setTargetNodeKey(trimToNull(item.getTargetNodeKey()));
            result.add(item);
        }
        return result;
    }
    private void validateGraph(List<WfTaskNodeEditorDTO> nodes, List<WfTaskEdgeDTO> edges) {
        if (nodes.isEmpty()) {
            throw new BusinessException("流程图不能为空");
        }

        Map<String, WfTaskNodeEditorDTO> nodeMap = new LinkedHashMap<>();
        for (WfTaskNodeEditorDTO node : nodes) {
            if (!StringUtils.hasText(node.getNodeKey())) {
                throw new BusinessException("节点标识不能为空");
            }
            if (nodeMap.put(node.getNodeKey(), node) != null) {
                throw new BusinessException("节点标识重复：" + node.getNodeKey());
            }
            if (!isSupportedNodeType(node.getNodeType())) {
                throw new BusinessException("存在不支持的节点类型");
            }
        }

        Map<String, List<String>> outgoing = new LinkedHashMap<>();
        Map<String, List<String>> incoming = new LinkedHashMap<>();
        for (String nodeKey : nodeMap.keySet()) {
            outgoing.put(nodeKey, new ArrayList<>());
            incoming.put(nodeKey, new ArrayList<>());
        }

        Set<String> edgeKeys = new HashSet<>();
        for (WfTaskEdgeDTO edge : edges) {
            if (!StringUtils.hasText(edge.getSourceNodeKey()) || !StringUtils.hasText(edge.getTargetNodeKey())) {
                throw new BusinessException("连线缺少起点或终点");
            }
            if (!nodeMap.containsKey(edge.getSourceNodeKey()) || !nodeMap.containsKey(edge.getTargetNodeKey())) {
                throw new BusinessException("连线引用了不存在的节点");
            }
            if (Objects.equals(edge.getSourceNodeKey(), edge.getTargetNodeKey())) {
                throw new BusinessException("节点不允许自循环");
            }
            String edgeKey = edge.getSourceNodeKey() + "->" + edge.getTargetNodeKey();
            if (!edgeKeys.add(edgeKey)) {
                throw new BusinessException("存在重复连线");
            }
            outgoing.get(edge.getSourceNodeKey()).add(edge.getTargetNodeKey());
            incoming.get(edge.getTargetNodeKey()).add(edge.getSourceNodeKey());
        }

        List<WfTaskNodeEditorDTO> startNodes = filterByNodeType(nodes, WorkflowConstants.NodeType.START);
        List<WfTaskNodeEditorDTO> endNodes = filterByNodeType(nodes, WorkflowConstants.NodeType.END);
        if (startNodes.size() != 1) {
            throw new BusinessException("流程必须且只能有一个开始节点");
        }
        if (endNodes.size() != 1) {
            throw new BusinessException("流程必须且只能有一个结束节点");
        }

        for (WfTaskNodeEditorDTO node : nodes) {
            List<String> predecessors = incoming.get(node.getNodeKey());
            List<String> successors = outgoing.get(node.getNodeKey());
            Integer nodeType = node.getNodeType();

            if (Objects.equals(nodeType, WorkflowConstants.NodeType.START)) {
                if (!predecessors.isEmpty()) {
                    throw new BusinessException("开始节点不能有入边");
                }
                if (successors.size() != 1) {
                    throw new BusinessException("开始节点必须有且仅有一条出边");
                }
                continue;
            }

            if (Objects.equals(nodeType, WorkflowConstants.NodeType.END)) {
                if (!successors.isEmpty()) {
                    throw new BusinessException("结束节点不能有出边");
                }
                continue;
            }

            if (Objects.equals(nodeType, WorkflowConstants.NodeType.APPROVE)) {
                if (successors.size() != 1) {
                    throw new BusinessException("审批节点必须有且仅有一条出边");
                }
                if (node.getApproveType() == null) {
                    throw new BusinessException("审批节点必须配置审批类型");
                }
                boolean hasApprover = node.getApprovers().stream()
                        .anyMatch(item -> item.getApproverType() != null && !item.getApproverIds().isEmpty());
                if (!hasApprover) {
                    throw new BusinessException("审批节点必须配置审批人");
                }
                continue;
            }

            if (Objects.equals(nodeType, WorkflowConstants.NodeType.BRANCH)) {
                if (successors.size() < 2) {
                    throw new BusinessException("条件分支节点至少需要两条出边");
                }
                if (!StringUtils.hasText(node.getDefaultBranchNodeKey())) {
                    throw new BusinessException("条件分支节点必须配置默认分支");
                }
                if (!successors.contains(node.getDefaultBranchNodeKey())) {
                    throw new BusinessException("条件分支默认去向必须存在于出边中");
                }
                if (node.getBranchRules().isEmpty()) {
                    throw new BusinessException("条件分支节点至少需要一条规则");
                }
                for (WfBranchRuleDTO rule : node.getBranchRules()) {
                    if (!StringUtils.hasText(rule.getFieldKey())) {
                        throw new BusinessException("条件分支规则缺少字段键");
                    }
                    if (!StringUtils.hasText(rule.getOperator())) {
                        throw new BusinessException("条件分支规则缺少操作符");
                    }
                    if (!StringUtils.hasText(rule.getNextNodeKey())) {
                        throw new BusinessException("条件分支规则缺少目标节点");
                    }
                    if (!successors.contains(rule.getNextNodeKey())) {
                        throw new BusinessException("条件分支规则目标节点必须存在于出边中");
                    }
                }
            }
        }

        String startNodeKey = startNodes.get(0).getNodeKey();
        String endNodeKey = endNodes.get(0).getNodeKey();
        ensureAcyclic(nodeMap.keySet(), outgoing, incoming);
        ensureReachability(startNodeKey, endNodeKey, nodeMap.keySet(), outgoing, incoming);
    }
    private boolean isSupportedNodeType(Integer nodeType) {
        return Objects.equals(nodeType, WorkflowConstants.NodeType.START)
                || Objects.equals(nodeType, WorkflowConstants.NodeType.END)
                || Objects.equals(nodeType, WorkflowConstants.NodeType.APPROVE)
                || Objects.equals(nodeType, WorkflowConstants.NodeType.BRANCH);
    }
    private List<WfTaskNodeEditorDTO> filterByNodeType(List<WfTaskNodeEditorDTO> nodes, Integer nodeType) {
        return nodes.stream()
                .filter(item -> Objects.equals(nodeType, item.getNodeType()))
                .collect(Collectors.toList());
    }
    private void ensureAcyclic(Set<String> nodeKeys, Map<String, List<String>> outgoing, Map<String, List<String>> incoming) {
        Map<String, Integer> indegree = new HashMap<>();
        for (String nodeKey : nodeKeys) {
            indegree.put(nodeKey, incoming.getOrDefault(nodeKey, Collections.emptyList()).size());
        }

        Deque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        int visited = 0;
        while (!queue.isEmpty()) {
            String nodeKey = queue.poll();
            visited++;
            for (String nextNodeKey : outgoing.getOrDefault(nodeKey, Collections.emptyList())) {
                int nextIndegree = indegree.get(nextNodeKey) - 1;
                indegree.put(nextNodeKey, nextIndegree);
                if (nextIndegree == 0) {
                    queue.offer(nextNodeKey);
                }
            }
        }

        if (visited != nodeKeys.size()) {
            throw new BusinessException("流程图不能存在环");
        }
    }
    private void ensureReachability(String startNodeKey, String endNodeKey, Set<String> nodeKeys, Map<String, List<String>> outgoing, Map<String, List<String>> incoming) {
        Set<String> reachableFromStart = traverse(startNodeKey, outgoing);
        if (reachableFromStart.size() != nodeKeys.size()) {
            throw new BusinessException("流程图存在从开始节点不可达的孤立节点");
        }
        Set<String> reachableToEnd = traverse(endNodeKey, incoming);
        if (reachableToEnd.size() != nodeKeys.size()) {
            throw new BusinessException("流程图存在无法到达结束节点的节点");
        }
    }
    private Set<String> traverse(String startNodeKey, Map<String, List<String>> relationMap) {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.offer(startNodeKey);
        visited.add(startNodeKey);
        while (!queue.isEmpty()) {
            String nodeKey = queue.poll();
            for (String nextNodeKey : relationMap.getOrDefault(nodeKey, Collections.emptyList())) {
                if (visited.add(nextNodeKey)) {
                    queue.offer(nextNodeKey);
                }
            }
        }
        return visited;
    }
    private Map<String, List<String>> buildRelationMap(List<WfTaskEdgeDTO> edges, boolean outgoing) {
        Map<String, List<String>> relationMap = new LinkedHashMap<>();
        for (WfTaskEdgeDTO edge : edges) {
            String from = outgoing ? edge.getSourceNodeKey() : edge.getTargetNodeKey();
            String to = outgoing ? edge.getTargetNodeKey() : edge.getSourceNodeKey();
            relationMap.computeIfAbsent(from, key -> new ArrayList<>()).add(to);
        }
        return relationMap;
    }
    private Map<String, Integer> calculateNodeLevels(List<WfTaskNodeEditorDTO> nodes, Map<String, List<String>> outgoing, Map<String, List<String>> incoming) {
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, Integer> levels = new HashMap<>();
        String startNodeKey = null;
        for (WfTaskNodeEditorDTO node : nodes) {
            indegree.put(node.getNodeKey(), incoming.getOrDefault(node.getNodeKey(), Collections.emptyList()).size());
            if (Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.START)) {
                startNodeKey = node.getNodeKey();
                levels.put(node.getNodeKey(), 1);
            }
        }

        Deque<String> queue = new ArrayDeque<>();
        if (startNodeKey != null) {
            queue.offer(startNodeKey);
        }
        while (!queue.isEmpty()) {
            String nodeKey = queue.poll();
            int currentLevel = levels.getOrDefault(nodeKey, 1);
            for (String nextNodeKey : outgoing.getOrDefault(nodeKey, Collections.emptyList())) {
                levels.put(nextNodeKey, Math.max(levels.getOrDefault(nextNodeKey, 1), currentLevel + 1));
                int nextIndegree = indegree.get(nextNodeKey) - 1;
                indegree.put(nextNodeKey, nextIndegree);
                if (nextIndegree == 0) {
                    queue.offer(nextNodeKey);
                }
            }
        }
        return levels;
    }
    private Integer resolvePreLevel(List<String> predecessors, Map<String, Integer> levels) {
        return predecessors.stream().map(levels::get).filter(Objects::nonNull).max(Integer::compareTo).orElse(0);
    }
    private Integer resolveNextLevel(List<String> successors, Map<String, Integer> levels) {
        return successors.stream().map(levels::get).filter(Objects::nonNull).min(Integer::compareTo).orElse(0);
    }
    private String buildBranchConditions(WfTaskNodeEditorDTO node, Map<String, WfTaskNodeConfig> persistedNodeMap) {
        JSONObject branch = new JSONObject();
        JSONArray conditions = new JSONArray();
        for (WfBranchRuleDTO rule : node.getBranchRules()) {
            JSONObject item = new JSONObject();
            item.put("field", rule.getFieldKey());
            item.put("fieldKey", rule.getFieldKey());
            item.put("fieldLabel", rule.getFieldLabel());
            item.put("operator", rule.getOperator());
            item.put("value", rule.getValue());
            item.put("nextNodeId", persistedNodeMap.get(rule.getNextNodeKey()).getId());
            item.put("nextNodeKey", rule.getNextNodeKey());
            conditions.add(item);
        }
        branch.put("conditions", conditions);
        branch.put("defaultNodeId", persistedNodeMap.get(node.getDefaultBranchNodeKey()).getId());
        branch.put("defaultNodeKey", node.getDefaultBranchNodeKey());
        return branch.toJSONString();
    }
    private List<Long> toNodeIds(List<String> nodeKeys, Map<String, WfTaskNodeConfig> persistedNodeMap) {
        return nodeKeys.stream()
                .map(persistedNodeMap::get)
                .filter(Objects::nonNull)
                .map(WfTaskNodeConfig::getId)
                .collect(Collectors.toList());
    }
    private void markTaskNodesDeleted(Long taskConfigId, Long tenantId) {
        List<WfTaskNodeConfig> nodes = listActiveNodes(taskConfigId, tenantId);
        if (nodes.isEmpty()) {
            return;
        }
        List<Long> nodeIds = nodes.stream().map(WfTaskNodeConfig::getId).collect(Collectors.toList());
        markNodeApproversDeleted(nodeIds, tenantId);
        for (WfTaskNodeConfig node : nodes) {
            WfTaskNodeConfig update = new WfTaskNodeConfig();
            update.setId(node.getId());
            update.setDeleted(1);
            nodeConfigMapper.updateById(update);
        }
    }
    private void markNodeApproversDeleted(List<Long> nodeIds, Long tenantId) {
        if (nodeIds.isEmpty()) {
            return;
        }
        List<WfTaskNodeApprover> approvers = listActiveApprovers(nodeIds, tenantId);
        for (WfTaskNodeApprover approver : approvers) {
            WfTaskNodeApprover update = new WfTaskNodeApprover();
            update.setId(approver.getId());
            update.setDeleted(1);
            nodeApproverMapper.updateById(update);
        }
    }
    private List<WfTaskNodeConfig> listActiveNodes(Long taskConfigId, Long tenantId) {
        return nodeConfigMapper.selectList(new LambdaQueryWrapper<WfTaskNodeConfig>()
                .eq(WfTaskNodeConfig::getTaskConfigId, taskConfigId)
                .eq(WfTaskNodeConfig::getTenantId, tenantId)
                .eq(WfTaskNodeConfig::getDeleted, 0)
                .orderByAsc(WfTaskNodeConfig::getOrderNum)
                .orderByAsc(WfTaskNodeConfig::getId));
    }
    private List<WfTaskNodeApprover> listActiveApprovers(List<Long> nodeIds, Long tenantId) {
        if (nodeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return nodeApproverMapper.selectList(new LambdaQueryWrapper<WfTaskNodeApprover>()
                .in(WfTaskNodeApprover::getNodeConfigId, nodeIds)
                .eq(WfTaskNodeApprover::getTenantId, tenantId)
                .eq(WfTaskNodeApprover::getDeleted, 0)
                .orderByAsc(WfTaskNodeApprover::getId));
    }
    private WfTaskConfig resolveOrCreateDraft(WfTaskDraftEditorQueryParam param, Long tenantId) {
        WfTaskConfig draft = resolveDraftIfExists(param, tenantId);
        if (draft != null) {
            return draft;
        }
        String taskCode = resolveTaskCode(param, tenantId);
        WfTaskConfig published = findPublishedByTaskCode(taskCode, tenantId);
        if (published == null) {
            throw new BusinessException("草稿不存在，请先保存基础信息");
        }
        return cloneAsDraft(published, tenantId);
    }
    private WfTaskConfig resolveExistingDraft(WfTaskDraftEditorQueryParam param, Long tenantId) {
        WfTaskConfig draft = resolveDraftIfExists(param, tenantId);
        if (draft == null) {
            throw new BusinessException("草稿不存在");
        }
        return draft;
    }
    private WfTaskConfig resolveDraftIfExists(WfTaskDraftEditorQueryParam param, Long tenantId) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (param.getId() != null) {
            WfTaskConfig config = requireTaskConfig(param.getId(), tenantId);
            if (Objects.equals(config.getConfigStage(), WorkflowConstants.ConfigStage.DRAFT)) {
                return config;
            }
            return findDraftByTaskCode(config.getTaskCode(), tenantId);
        }
        if (StringUtils.hasText(param.getTaskCode())) {
            return findDraftByTaskCode(param.getTaskCode().trim(), tenantId);
        }
        throw new BusinessException("任务标识不能为空");
    }
    private String resolveTaskCode(WfTaskDraftEditorQueryParam param, Long tenantId) {
        if (param.getId() != null) {
            return requireTaskConfig(param.getId(), tenantId).getTaskCode();
        }
        if (StringUtils.hasText(param.getTaskCode())) {
            return param.getTaskCode().trim();
        }
        throw new BusinessException("任务标识不能为空");
    }
    private WfTaskConfig cloneAsDraft(WfTaskConfig published, Long tenantId) {
        WfTaskConfig draft = new WfTaskConfig();
        BeanUtils.copyProperties(published, draft);
        draft.setId(null);
        draft.setCreateTime(null);
        draft.setCreateBy(null);
        draft.setUpdateTime(null);
        draft.setUpdateBy(null);
        draft.setDeleted(false);
        draft.setConfigStage(WorkflowConstants.ConfigStage.DRAFT);
        draft.setVersion(nextVersion(published.getTaskCode(), tenantId));
        taskConfigMapper.insert(draft);

        WfTaskGraphDTO publishedGraph = sanitizeGraphForDraftClone(buildGraph(published, tenantId));
        if (!publishedGraph.getNodes().isEmpty()) {
            persistGraph(
                    draft,
                    normalizeNodes(publishedGraph.getNodes()),
                    normalizeEdges(publishedGraph.getEdges()),
                    tenantId,
                    false
            );
        }
        return draft;
    }
    private WfTaskGraphDTO sanitizeGraphForDraftClone(WfTaskGraphDTO graph) {
        if (graph == null) {
            return null;
        }
        List<WfTaskNodeEditorDTO> nodes = normalizeNodes(graph.getNodes());
        List<WfTaskEdgeDTO> edges = normalizeEdges(graph.getEdges());
        Map<String, String> replacedNodeKeys = new LinkedHashMap<>();

        mergeDuplicateBoundaryNodes(nodes, edges, WorkflowConstants.NodeType.START, replacedNodeKeys);
        mergeDuplicateBoundaryNodes(nodes, edges, WorkflowConstants.NodeType.END, replacedNodeKeys);
        remapNodeReferences(nodes, edges, replacedNodeKeys);

        graph.setNodes(nodes);
        graph.setEdges(edges);
        if (!replacedNodeKeys.isEmpty()) {
            log.warn("克隆已发布流程为草稿时自动修复了边界节点异常，taskCode={}, replacements={}",
                    graph.getTaskCode(), replacedNodeKeys);
        }
        return graph;
    }
    private void mergeDuplicateBoundaryNodes(List<WfTaskNodeEditorDTO> nodes, List<WfTaskEdgeDTO> edges, Integer nodeType,
                                             Map<String, String> replacedNodeKeys) {
        List<WfTaskNodeEditorDTO> sameTypeNodes = filterByNodeType(nodes, nodeType);
        if (sameTypeNodes.size() <= 1) {
            return;
        }

        String keepNodeKey = sameTypeNodes.get(0).getNodeKey();
        Set<String> removedNodeKeys = sameTypeNodes.stream()
                .skip(1)
                .map(WfTaskNodeEditorDTO::getNodeKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (removedNodeKeys.isEmpty()) {
            return;
        }

        for (String removedNodeKey : removedNodeKeys) {
            replacedNodeKeys.put(removedNodeKey, keepNodeKey);
        }

        List<WfTaskEdgeDTO> mergedEdges = new ArrayList<>();
        Set<String> edgeKeys = new LinkedHashSet<>();
        for (WfTaskEdgeDTO edge : edges) {
            if (edge == null) {
                continue;
            }
            String sourceNodeKey = edge.getSourceNodeKey();
            String targetNodeKey = edge.getTargetNodeKey();

            if (Objects.equals(nodeType, WorkflowConstants.NodeType.START) && removedNodeKeys.contains(sourceNodeKey)) {
                sourceNodeKey = keepNodeKey;
            }
            if (Objects.equals(nodeType, WorkflowConstants.NodeType.END) && removedNodeKeys.contains(targetNodeKey)) {
                targetNodeKey = keepNodeKey;
            }
            if (removedNodeKeys.contains(sourceNodeKey) || removedNodeKeys.contains(targetNodeKey)) {
                continue;
            }
            if (!StringUtils.hasText(sourceNodeKey) || !StringUtils.hasText(targetNodeKey)
                    || Objects.equals(sourceNodeKey, targetNodeKey)) {
                continue;
            }
            String edgeKey = sourceNodeKey + "->" + targetNodeKey;
            if (!edgeKeys.add(edgeKey)) {
                continue;
            }
            WfTaskEdgeDTO mergedEdge = new WfTaskEdgeDTO();
            mergedEdge.setId(edge.getId());
            mergedEdge.setSourceNodeKey(sourceNodeKey);
            mergedEdge.setTargetNodeKey(targetNodeKey);
            mergedEdges.add(mergedEdge);
        }

        edges.clear();
        edges.addAll(mergedEdges);
        nodes.removeIf(node -> removedNodeKeys.contains(node.getNodeKey()));
    }
    private void remapNodeReferences(List<WfTaskNodeEditorDTO> nodes, List<WfTaskEdgeDTO> edges, Map<String, String> replacedNodeKeys) {
        if (replacedNodeKeys.isEmpty()) {
            return;
        }
        for (WfTaskNodeEditorDTO node : nodes) {
            if (node == null) {
                continue;
            }
            node.setDefaultBranchNodeKey(resolveReplacement(node.getDefaultBranchNodeKey(), replacedNodeKeys));
            List<WfBranchRuleDTO> branchRules = node.getBranchRules();
            if (branchRules == null) {
                continue;
            }
            for (WfBranchRuleDTO rule : branchRules) {
                if (rule == null) {
                    continue;
                }
                rule.setNextNodeKey(resolveReplacement(rule.getNextNodeKey(), replacedNodeKeys));
            }
        }
        for (WfTaskEdgeDTO edge : edges) {
            if (edge == null) {
                continue;
            }
            edge.setSourceNodeKey(resolveReplacement(edge.getSourceNodeKey(), replacedNodeKeys));
            edge.setTargetNodeKey(resolveReplacement(edge.getTargetNodeKey(), replacedNodeKeys));
        }
    }
    private String resolveReplacement(String nodeKey, Map<String, String> replacedNodeKeys) {
        if (!StringUtils.hasText(nodeKey)) {
            return nodeKey;
        }
        return replacedNodeKeys.getOrDefault(nodeKey, nodeKey);
    }
    private boolean existsVisibleTaskCode(String taskCode, Long tenantId) {
        return taskConfigMapper.selectCount(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getTaskCode, taskCode)
                .eq(WfTaskConfig::getDeleted, false)
                .in(WfTaskConfig::getConfigStage, VISIBLE_STAGES)) > 0;
    }
    private Integer nextVersion(String taskCode, Long tenantId) {
        WfTaskConfig latest = taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getTaskCode, taskCode)
                .eq(WfTaskConfig::getDeleted, false)
                .orderByDesc(WfTaskConfig::getVersion)
                .last("LIMIT 1"));
        return latest == null || latest.getVersion() == null ? 1 : latest.getVersion() + 1;
    }
    private WfTaskConfig requireTaskConfig(Long id, Long tenantId) {
        WfTaskConfig config = taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getId, id)
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getDeleted, false)
                .last("LIMIT 1"));
        if (config == null) {
            throw new BusinessException("审批任务配置不存在");
        }
        return config;
    }
    private WfTaskConfig requireDraftById(Long id, Long tenantId) {
        WfTaskConfig draft = taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getId, id)
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getDeleted, false)
                .eq(WfTaskConfig::getConfigStage, WorkflowConstants.ConfigStage.DRAFT)
                .last("LIMIT 1"));
        if (draft == null) {
            throw new BusinessException("草稿不存在");
        }
        return draft;
    }
    private WfTaskConfig findDraftByTaskCode(String taskCode, Long tenantId) {
        if (!StringUtils.hasText(taskCode)) {
            return null;
        }
        return taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getTaskCode, taskCode)
                .eq(WfTaskConfig::getDeleted, false)
                .eq(WfTaskConfig::getConfigStage, WorkflowConstants.ConfigStage.DRAFT)
                .orderByDesc(WfTaskConfig::getVersion)
                .last("LIMIT 1"));
    }
    private WfTaskConfig findPublishedByTaskCode(String taskCode, Long tenantId) {
        if (!StringUtils.hasText(taskCode)) {
            return null;
        }
        return taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getTaskCode, taskCode)
                .eq(WfTaskConfig::getDeleted, false)
                .eq(WfTaskConfig::getConfigStage, WorkflowConstants.ConfigStage.PUBLISHED)
                .orderByDesc(WfTaskConfig::getVersion)
                .last("LIMIT 1"));
    }
    private String resolveNodeKey(WfTaskNodeConfig node) {
        return StringUtils.hasText(node.getNodeKey()) ? node.getNodeKey() : "node_" + node.getId();
    }
    private String resolveNodeName(WfTaskNodeEditorDTO node) {
        if (StringUtils.hasText(node.getNodeName())) {
            return node.getNodeName();
        }
        if (Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.START)) {
            return "开始";
        }
        if (Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.END)) {
            return "结束";
        }
        if (Objects.equals(node.getNodeType(), WorkflowConstants.NodeType.APPROVE)) {
            return "审批节点";
        }
        return "条件分支";
    }
    private String buildNodeI18n(String nodeName) {
        JSONObject i18n = new JSONObject();
        i18n.put("zh-CN", nodeName);
        i18n.put("en-US", nodeName);
        return i18n.toJSONString();
    }
    private List<Long> parseLongArray(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        JSONArray array = JSON.parseArray(json);
        if (array == null || array.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> values = new ArrayList<>();
        for (Object item : array) {
            if (item == null) {
                continue;
            }
            values.add(Long.valueOf(item.toString()));
        }
        return values;
    }
    private String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
    private Long requireCurrentTenantId() {
        Long tenantId = CurrentUserUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到当前租户信息");
        }
        return tenantId;
    }
    private WfTaskConfigDTO convertToDTO(WfTaskConfig config) {
        WfTaskConfigDTO dto = new WfTaskConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }
}
