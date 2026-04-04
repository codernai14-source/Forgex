package com.forgex.workflow.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.tenant.TenantContextIgnore;
import com.forgex.workflow.common.WorkflowConstants;
import com.forgex.workflow.domain.dto.WorkflowAdminSeedUserDTO;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskNodeApprover;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskNodeApproverMapper;
import com.forgex.workflow.mapper.WfTaskNodeConfigMapper;
import com.forgex.workflow.mapper.WorkflowAdminSeedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 初始化工作流演示任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowDemoTaskInitializer {

    private static final String TASK_CODE = "LEAVE_APPROVAL_DEMO";
    private static final String TASK_NAME = "请假审批";
    private static final String INTERPRETER_BEAN = "leaveApprovalInterpreter";
    private static final String FORM_PATH = "/workflow/form/leave";

    private final WorkflowAdminSeedMapper workflowAdminSeedMapper;
    private final WfTaskConfigMapper taskConfigMapper;
    private final WfTaskNodeConfigMapper nodeConfigMapper;
    private final WfTaskNodeApproverMapper nodeApproverMapper;

    /**
     * 应用启动后补齐演示审批任务。
     * <p>
     * 使用 @DSTransactional 支持多数据源事务切换：
     * - admin 数据源：查询管理员用户信息
     * - workflow 数据源：操作工作流配置表
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    @DSTransactional(rollbackFor = Exception.class)
    public void initializeDemoTask() {
        TenantContextIgnore.setIgnore(true);
        try {
            List<WorkflowAdminSeedUserDTO> adminUsers = workflowAdminSeedMapper.listActiveAdminUsers();
            if (adminUsers == null || adminUsers.isEmpty()) {
                log.warn("未查询到可用的 admin 账号，跳过请假审批演示任务初始化");
                return;
            }

            Map<Long, WorkflowAdminSeedUserDTO> adminByTenant = adminUsers.stream()
                    .filter(item -> item.getTenantId() != null && item.getUserId() != null)
                    .collect(Collectors.toMap(
                            WorkflowAdminSeedUserDTO::getTenantId,
                            item -> item,
                            this::pickPreferredAdmin
                    ));

            for (WorkflowAdminSeedUserDTO adminUser : adminByTenant.values()) {
                ensureLeaveDemoTask(adminUser);
            }

            log.info("请假审批演示任务初始化完成，租户数={}", adminByTenant.size());
        } catch (Exception ex) {
            log.error("初始化请假审批演示任务失败", ex);
        } finally {
            TenantContextIgnore.clear();
        }
    }

    private void ensureLeaveDemoTask(WorkflowAdminSeedUserDTO adminUser) {
        Long tenantId = adminUser.getTenantId();
        Long adminUserId = adminUser.getUserId();

        WfTaskConfig taskConfig = ensureTaskConfig(tenantId);
        WfTaskNodeConfig startNode = ensureNode(taskConfig.getId(), tenantId, 1, 1, "发起请假");
        WfTaskNodeConfig approveNode = ensureNode(taskConfig.getId(), tenantId, 3, 2, "管理员审批");
        WfTaskNodeConfig endNode = ensureNode(taskConfig.getId(), tenantId, 2, 3, "审批结束");

        startNode.setNodeLevel(1);
        startNode.setPreLevel(0);
        startNode.setPreNodeIds("[]");
        startNode.setNextLevel(2);
        startNode.setNextNodeIds(JSON.toJSONString(List.of(approveNode.getId())));
        nodeConfigMapper.updateById(startNode);

        approveNode.setNodeLevel(2);
        approveNode.setPreLevel(1);
        approveNode.setPreNodeIds(JSON.toJSONString(List.of(startNode.getId())));
        approveNode.setNextLevel(3);
        approveNode.setNextNodeIds(JSON.toJSONString(List.of(endNode.getId())));
        approveNode.setApproveType(2);
        nodeConfigMapper.updateById(approveNode);

        endNode.setNodeLevel(3);
        endNode.setPreLevel(2);
        endNode.setPreNodeIds(JSON.toJSONString(List.of(approveNode.getId())));
        endNode.setNextLevel(0);
        endNode.setNextNodeIds("[]");
        nodeConfigMapper.updateById(endNode);

        ensureApprover(approveNode.getId(), tenantId, adminUserId);
    }

    private WfTaskConfig ensureTaskConfig(Long tenantId) {
        WfTaskConfig taskConfig = taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTenantId, tenantId)
                .eq(WfTaskConfig::getTaskCode, TASK_CODE)
                .eq(WfTaskConfig::getDeleted, false)
                .eq(WfTaskConfig::getConfigStage, WorkflowConstants.ConfigStage.PUBLISHED)
                .orderByDesc(WfTaskConfig::getVersion)
                .last("LIMIT 1"));

        boolean isNew = taskConfig == null;
        if (isNew) {
            taskConfig = new WfTaskConfig();
            taskConfig.setTenantId(tenantId);
            taskConfig.setVersion(1);
        }

        JSONObject taskNameI18n = new JSONObject();
        taskNameI18n.put("zh-CN", TASK_NAME);
        taskNameI18n.put("en-US", "Leave Approval");

        taskConfig.setTaskName(TASK_NAME);
        taskConfig.setTaskNameI18nJson(taskNameI18n.toJSONString());
        taskConfig.setTaskCode(TASK_CODE);
        taskConfig.setInterpreterBean(INTERPRETER_BEAN);
        taskConfig.setFormType(1);
        taskConfig.setFormPath(FORM_PATH);
        taskConfig.setStatus(1);
        taskConfig.setConfigStage(WorkflowConstants.ConfigStage.PUBLISHED);
        taskConfig.setRemark("演示流程：用户发起 -> admin 审核 -> 结束");

        if (isNew) {
            taskConfigMapper.insert(taskConfig);
        } else {
            taskConfigMapper.updateById(taskConfig);
        }

        return taskConfig;
    }

    private WfTaskNodeConfig ensureNode(Long taskConfigId, Long tenantId, Integer nodeType, Integer orderNum, String nodeName) {
        String nodeKey = resolveNodeKey(nodeType, orderNum);
        WfTaskNodeConfig node = nodeConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskNodeConfig>()
                .eq(WfTaskNodeConfig::getTaskConfigId, taskConfigId)
                .eq(WfTaskNodeConfig::getTenantId, tenantId)
                .eq(WfTaskNodeConfig::getNodeKey, nodeKey)
                .eq(WfTaskNodeConfig::getDeleted, 0)
                .orderByAsc(WfTaskNodeConfig::getOrderNum)
                .last("LIMIT 1"));

        boolean isNew = node == null;
        if (isNew) {
            node = new WfTaskNodeConfig();
            node.setTaskConfigId(taskConfigId);
            node.setTenantId(tenantId);
            node.setNodeType(nodeType);
            node.setNodeKey(nodeKey);
            node.setOrderNum(orderNum);
            node.setNodeName(nodeName);
            node.setNodeNameI18nJson(buildNodeI18n(nodeName));
            node.setCanvasX(resolveCanvasX(orderNum));
            node.setCanvasY(240D);
            nodeConfigMapper.insert(node);
            return node;
        }

        node.setNodeType(nodeType);
        node.setNodeKey(nodeKey);
        node.setOrderNum(orderNum);
        node.setNodeName(nodeName);
        node.setNodeNameI18nJson(buildNodeI18n(nodeName));
        node.setCanvasX(resolveCanvasX(orderNum));
        node.setCanvasY(240D);
        nodeConfigMapper.updateById(node);
        return node;
    }

    private void ensureApprover(Long nodeConfigId, Long tenantId, Long approverId) {
        WfTaskNodeApprover approver = nodeApproverMapper.selectOne(new LambdaQueryWrapper<WfTaskNodeApprover>()
                .eq(WfTaskNodeApprover::getNodeConfigId, nodeConfigId)
                .eq(WfTaskNodeApprover::getTenantId, tenantId)
                .eq(WfTaskNodeApprover::getDeleted, 0)
                .orderByAsc(WfTaskNodeApprover::getId)
                .last("LIMIT 1"));

        boolean isNew = approver == null;
        if (isNew) {
            approver = new WfTaskNodeApprover();
            approver.setNodeConfigId(nodeConfigId);
            approver.setTenantId(tenantId);
        }

        approver.setApproverType(1);
        approver.setApproverIds(JSON.toJSONString(List.of(approverId)));

        if (isNew) {
            nodeApproverMapper.insert(approver);
        } else {
            nodeApproverMapper.updateById(approver);
        }
    }

    private WorkflowAdminSeedUserDTO pickPreferredAdmin(WorkflowAdminSeedUserDTO left, WorkflowAdminSeedUserDTO right) {
        if (isAdminAccount(left) && !isAdminAccount(right)) {
            return left;
        }
        if (!isAdminAccount(left) && isAdminAccount(right)) {
            return right;
        }
        return Arrays.asList(left, right).stream()
                .filter(Objects::nonNull)
                .min(Comparator.comparing(WorkflowAdminSeedUserDTO::getUserId))
                .orElse(left);
    }

    private boolean isAdminAccount(WorkflowAdminSeedUserDTO user) {
        return user != null && StringUtils.hasText(user.getAccount()) && "admin".equalsIgnoreCase(user.getAccount());
    }

    private String resolveNodeKey(Integer nodeType, Integer orderNum) {
        if (Objects.equals(nodeType, 1)) {
            return "start";
        }
        if (Objects.equals(nodeType, 2)) {
            return "end";
        }
        return "approve_" + orderNum;
    }

    private Double resolveCanvasX(Integer orderNum) {
        return 120D + (Math.max(orderNum, 1) - 1) * 300D;
    }

    private String buildNodeI18n(String zhCnName) {
        JSONObject i18n = new JSONObject();
        i18n.put("zh-CN", zhCnName);
        i18n.put("en-US", zhCnName);
        return i18n.toJSONString();
    }
}
