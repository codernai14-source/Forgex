/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.workflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.api.service.UserInfoService;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.workflow.domain.dto.WfExecutionDTO;
import com.forgex.workflow.domain.entity.WfMyTask;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionApprover;
import com.forgex.workflow.domain.entity.WfTaskExecutionDetail;
import com.forgex.workflow.domain.param.WfExecutionApproveParam;
import com.forgex.workflow.domain.param.WfExecutionQueryParam;
import com.forgex.workflow.domain.param.WfExecutionStartParam;
import com.forgex.workflow.mapper.WfMyTaskMapper;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionApproverMapper;
import com.forgex.workflow.mapper.WfTaskExecutionDetailMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.service.IWfEngineService;
import com.forgex.workflow.service.IWfExecutionService;
import com.forgex.workflow.service.interpreter.ApprovalContext;
import com.forgex.workflow.service.interpreter.ApprovalInterpreterRegistry;
import com.forgex.workflow.service.interpreter.IApprovalInterpreter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审批执行 Service 实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfExecutionServiceImpl implements IWfExecutionService {

    private static final DateTimeFormatter APPROVE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WfTaskExecutionMapper executionMapper;
    private final WfTaskExecutionDetailMapper executionDetailMapper;
    private final WfTaskExecutionApproverMapper executionApproverMapper;
    private final WfMyTaskMapper myTaskMapper;
    private final WfTaskConfigMapper taskConfigMapper;
    private final IWfEngineService engineService;
    private final UserInfoService userInfoService;
    private final ApprovalInterpreterRegistry interpreterRegistry;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startExecution(WfExecutionStartParam param) {
        Long currentUserId = requireCurrentUserId();
        Long currentTenantId = requireCurrentTenantId();

        WfTaskConfig taskConfig = taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
                .eq(WfTaskConfig::getTaskCode, param.getTaskCode())
                .eq(WfTaskConfig::getTenantId, currentTenantId)
                .eq(WfTaskConfig::getDeleted, false)
                .last("LIMIT 1"));
        if (taskConfig == null) {
            throw new BusinessException("审批任务配置不存在");
        }
        if (Objects.equals(taskConfig.getStatus(), 0)) {
            throw new BusinessException("审批任务已被禁用");
        }

        WfTaskExecution execution = new WfTaskExecution();
        execution.setTaskConfigId(taskConfig.getId());
        execution.setTaskCode(taskConfig.getTaskCode());
        execution.setTaskName(taskConfig.getTaskName());
        execution.setFormContent(param.getFormContent());
        execution.setStatus(1);
        execution.setStartTime(LocalDateTime.now());
        execution.setInitiatorId(currentUserId);
        execution.setInitiatorName(resolveUsername(currentUserId));
        execution.setTenantId(currentTenantId);
        executionMapper.insert(execution);

        engineService.initExecution(execution.getId(), taskConfig.getId());

        log.info("发起审批成功，executionId={}, taskCode={}, initiatorId={}",
                execution.getId(), taskConfig.getTaskCode(), currentUserId);
        return execution.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(WfExecutionApproveParam param) {
        Long currentUserId = requireCurrentUserId();
        WfTaskExecution execution = requireRunningExecution(param.getExecutionId());
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());

        Long currentNodeId = execution.getCurrentNodeId();
        String currentNodeName = execution.getCurrentNodeName();
        if (currentNodeId == null) {
            throw new BusinessException("当前审批节点不存在");
        }

        WfMyTask pendingTask = requirePendingTask(execution.getId(), currentNodeId, currentUserId);
        WfTaskExecutionDetail currentDetail = requireCurrentExecutionDetail(execution.getId(), currentNodeId);
        String approverName = resolveUsername(currentUserId);
        LocalDateTime approveTime = LocalDateTime.now();

        saveApprovalRecord(currentDetail, execution, currentUserId, approverName, 1, param.getComment(), null, approveTime);
        currentDetail.setCurrentStatus(1);
        executionDetailMapper.updateById(currentDetail);

        pendingTask.setStatus(1);
        myTaskMapper.updateById(pendingTask);

        callInterpreter(taskConfig == null ? null : taskConfig.getInterpreterBean(), (interpreter, context) -> {
            populateContext(context, execution, taskConfig, currentNodeId, currentNodeName);
            context.setApproverId(currentUserId);
            context.setApproverName(approverName);
            context.setApproveStatus(1);
            context.setComment(param.getComment());
            context.setApproveTime(approveTime);
            interpreter.onApprove(context);
        });

        if (Boolean.TRUE.equals(engineService.isNodeCompleted(execution.getId(), currentNodeId))) {
            engineService.moveToNextNode(execution.getId(), currentNodeId, 1, null);
        }

        log.info("审批同意成功，executionId={}, nodeId={}, approverId={}",
                execution.getId(), currentNodeId, currentUserId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(WfExecutionApproveParam param) {
        Long currentUserId = requireCurrentUserId();
        WfTaskExecution execution = requireRunningExecution(param.getExecutionId());
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());

        Long currentNodeId = execution.getCurrentNodeId();
        String currentNodeName = execution.getCurrentNodeName();
        if (currentNodeId == null) {
            throw new BusinessException("当前审批节点不存在");
        }

        Integer rejectType = param.getRejectType() == null ? 1 : param.getRejectType();
        WfMyTask pendingTask = requirePendingTask(execution.getId(), currentNodeId, currentUserId);
        WfTaskExecutionDetail currentDetail = requireCurrentExecutionDetail(execution.getId(), currentNodeId);
        String approverName = resolveUsername(currentUserId);
        LocalDateTime approveTime = LocalDateTime.now();

        saveApprovalRecord(currentDetail, execution, currentUserId, approverName, 2, param.getComment(), rejectType, approveTime);
        currentDetail.setCurrentStatus(2);
        executionDetailMapper.updateById(currentDetail);

        pendingTask.setStatus(1);
        myTaskMapper.updateById(pendingTask);

        callInterpreter(taskConfig == null ? null : taskConfig.getInterpreterBean(), (interpreter, context) -> {
            populateContext(context, execution, taskConfig, currentNodeId, currentNodeName);
            context.setApproverId(currentUserId);
            context.setApproverName(approverName);
            context.setApproveStatus(2);
            context.setComment(param.getComment());
            context.setApproveTime(approveTime);
            interpreter.onReject(context);
        });

        engineService.moveToNextNode(execution.getId(), currentNodeId, 2, rejectType);

        log.info("审批驳回成功，executionId={}, nodeId={}, approverId={}, rejectType={}",
                execution.getId(), currentNodeId, currentUserId, rejectType);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelExecution(Long executionId) {
        Long currentUserId = requireCurrentUserId();
        WfTaskExecution execution = requireRunningExecution(executionId);

        if (!Objects.equals(execution.getInitiatorId(), currentUserId)) {
            throw new BusinessException("仅发起人本人可以撤销审批");
        }

        engineService.finishExecution(executionId, 3);
        log.info("撤销审批成功，executionId={}, initiatorId={}", executionId, currentUserId);
        return true;
    }

    @Override
    public WfExecutionDTO getExecutionDetail(Long executionId) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        return execution == null ? null : convertToDTO(execution);
    }

    @Override
    public Page<WfExecutionDTO> pageMyInitiated(WfExecutionQueryParam param) {
        Page<WfTaskExecution> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<WfTaskExecution> wrapper = buildQueryWrapper(param);
        wrapper.eq(WfTaskExecution::getInitiatorId, requireCurrentUserId());
        wrapper.orderByDesc(WfTaskExecution::getStartTime);
        return convertToDTOPage(executionMapper.selectPage(page, wrapper));
    }

    @Override
    public Page<WfExecutionDTO> pageMyPending(WfExecutionQueryParam param) {
        Long currentUserId = requireCurrentUserId();
        List<WfMyTask> myTasks = myTaskMapper.selectList(new LambdaQueryWrapper<WfMyTask>()
                .eq(WfMyTask::getStatus, 0)
                .orderByDesc(WfMyTask::getCreateTime));

        List<Long> executionIds = myTasks.stream()
                .filter(task -> containsApprover(task.getApproverIds(), currentUserId))
                .map(WfMyTask::getExecutionId)
                .collect(Collectors.toList());

        return pageByExecutionIds(executionIds, param);
    }

    @Override
    public Page<WfExecutionDTO> pageMyProcessed(WfExecutionQueryParam param) {
        Long currentUserId = requireCurrentUserId();
        List<WfTaskExecutionApprover> approverRecords = executionApproverMapper.selectList(
                new LambdaQueryWrapper<WfTaskExecutionApprover>()
                        .orderByDesc(WfTaskExecutionApprover::getUpdateTime)
                        .orderByDesc(WfTaskExecutionApprover::getCreateTime));

        Set<Long> orderedExecutionIds = new LinkedHashSet<>();
        for (WfTaskExecutionApprover approverRecord : approverRecords) {
            if (containsApprovalDetail(approverRecord.getApproverDetail(), currentUserId)) {
                orderedExecutionIds.add(approverRecord.getExecutionId());
            }
        }
        return pageByExecutionIds(new ArrayList<>(orderedExecutionIds), param);
    }

    private Page<WfExecutionDTO> pageByExecutionIds(List<Long> executionIds, WfExecutionQueryParam param) {
        Page<WfExecutionDTO> page = new Page<>(param.getPageNum(), param.getPageSize());
        if (executionIds == null || executionIds.isEmpty()) {
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        }

        List<Long> orderedIds = executionIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<WfTaskExecution> executions = executionMapper.selectList(new LambdaQueryWrapper<WfTaskExecution>()
                .in(WfTaskExecution::getId, orderedIds)
                .eq(WfTaskExecution::getDeleted, 0));

        Map<Long, WfTaskExecution> executionMap = executions.stream()
                .collect(Collectors.toMap(WfTaskExecution::getId, item -> item));

        List<WfExecutionDTO> matchedRecords = orderedIds.stream()
                .map(executionMap::get)
                .filter(Objects::nonNull)
                .filter(execution -> matchesQuery(execution, param))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        int total = matchedRecords.size();
        int fromIndex = Math.max((param.getPageNum() - 1) * param.getPageSize(), 0);
        int toIndex = Math.min(fromIndex + param.getPageSize(), total);

        page.setTotal(total);
        if (fromIndex >= total) {
            page.setRecords(List.of());
            return page;
        }

        page.setRecords(matchedRecords.subList(fromIndex, toIndex));
        return page;
    }

    private boolean matchesQuery(WfTaskExecution execution, WfExecutionQueryParam param) {
        if (StringUtils.hasText(param.getTaskName())
                && !containsIgnoreCase(execution.getTaskName(), param.getTaskName())) {
            return false;
        }
        if (StringUtils.hasText(param.getTaskCode())
                && !containsIgnoreCase(execution.getTaskCode(), param.getTaskCode())) {
            return false;
        }
        return param.getStatus() == null || Objects.equals(execution.getStatus(), param.getStatus());
    }

    private boolean containsIgnoreCase(String source, String target) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(target)) {
            return false;
        }
        return source.toLowerCase().contains(target.toLowerCase());
    }

    private WfTaskExecution requireRunningExecution(Long executionId) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("审批执行记录不存在");
        }
        if (!Objects.equals(execution.getStatus(), 1)) {
            throw new BusinessException("当前审批不在审批中状态");
        }
        return execution;
    }

    private WfMyTask requirePendingTask(Long executionId, Long nodeId, Long approverId) {
        List<WfMyTask> myTasks = myTaskMapper.selectList(new LambdaQueryWrapper<WfMyTask>()
                .eq(WfMyTask::getExecutionId, executionId)
                .eq(WfMyTask::getNodeId, nodeId)
                .eq(WfMyTask::getStatus, 0)
                .orderByDesc(WfMyTask::getId));

        return myTasks.stream()
                .filter(task -> containsApprover(task.getApproverIds(), approverId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("当前用户没有该审批节点的待办任务"));
    }

    private WfTaskExecutionDetail requireCurrentExecutionDetail(Long executionId, Long nodeId) {
        WfTaskExecutionDetail detail = executionDetailMapper.selectOne(new LambdaQueryWrapper<WfTaskExecutionDetail>()
                .eq(WfTaskExecutionDetail::getExecutionId, executionId)
                .eq(WfTaskExecutionDetail::getNodeId, nodeId)
                .orderByDesc(WfTaskExecutionDetail::getId)
                .last("LIMIT 1"));

        if (detail == null) {
            throw new BusinessException("当前审批节点执行明细不存在");
        }
        return detail;
    }

    private void saveApprovalRecord(WfTaskExecutionDetail detail,
                                    WfTaskExecution execution,
                                    Long approverId,
                                    String approverName,
                                    Integer approveStatus,
                                    String comment,
                                    Integer rejectType,
                                    LocalDateTime approveTime) {
        WfTaskExecutionApprover approverRecord = executionApproverMapper.selectOne(
                new LambdaQueryWrapper<WfTaskExecutionApprover>()
                        .eq(WfTaskExecutionApprover::getExecutionId, execution.getId())
                        .eq(WfTaskExecutionApprover::getNodeId, detail.getNodeId())
                        .eq(WfTaskExecutionApprover::getExecutionDetailId, detail.getId())
                        .orderByDesc(WfTaskExecutionApprover::getId)
                        .last("LIMIT 1"));

        boolean isNew = approverRecord == null;
        if (isNew) {
            approverRecord = new WfTaskExecutionApprover();
            approverRecord.setExecutionId(execution.getId());
            approverRecord.setExecutionDetailId(detail.getId());
            approverRecord.setNodeId(detail.getNodeId());
            approverRecord.setTenantId(execution.getTenantId());
        }

        JSONArray approverDetails = StringUtils.hasText(approverRecord.getApproverDetail())
                ? JSON.parseArray(approverRecord.getApproverDetail())
                : new JSONArray();

        JSONObject approverDetail = new JSONObject();
        approverDetail.put("approverId", approverId);
        approverDetail.put("approverName", approverName);
        approverDetail.put("approveStatus", approveStatus);
        approverDetail.put("approveTime", approveTime.format(APPROVE_TIME_FORMATTER));
        approverDetail.put("comment", comment);
        approverDetails.add(approverDetail);

        approverRecord.setApproverDetail(approverDetails.toJSONString());
        approverRecord.setRejectType(rejectType);

        if (isNew) {
            executionApproverMapper.insert(approverRecord);
        } else {
            executionApproverMapper.updateById(approverRecord);
        }
    }

    private boolean containsApprover(String approverIdsJson, Long approverId) {
        if (!StringUtils.hasText(approverIdsJson) || approverId == null) {
            return false;
        }

        JSONArray approverIds = JSON.parseArray(approverIdsJson);
        if (approverIds == null || approverIds.isEmpty()) {
            return false;
        }

        for (int i = 0; i < approverIds.size(); i++) {
            if (Objects.equals(approverId, approverIds.getLong(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsApprovalDetail(String approverDetailJson, Long approverId) {
        if (!StringUtils.hasText(approverDetailJson) || approverId == null) {
            return false;
        }

        JSONArray approverDetails = JSON.parseArray(approverDetailJson);
        if (approverDetails == null || approverDetails.isEmpty()) {
            return false;
        }

        for (int i = 0; i < approverDetails.size(); i++) {
            JSONObject detail = approverDetails.getJSONObject(i);
            if (detail != null && Objects.equals(detail.getLong("approverId"), approverId)) {
                return true;
            }
        }
        return false;
    }

    private LambdaQueryWrapper<WfTaskExecution> buildQueryWrapper(WfExecutionQueryParam param) {
        LambdaQueryWrapper<WfTaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskExecution::getDeleted, 0);

        if (StringUtils.hasText(param.getTaskName())) {
            wrapper.like(WfTaskExecution::getTaskName, param.getTaskName());
        }
        if (StringUtils.hasText(param.getTaskCode())) {
            wrapper.like(WfTaskExecution::getTaskCode, param.getTaskCode());
        }
        if (param.getStatus() != null) {
            wrapper.eq(WfTaskExecution::getStatus, param.getStatus());
        }

        return wrapper;
    }

    private WfExecutionDTO convertToDTO(WfTaskExecution execution) {
        WfExecutionDTO dto = new WfExecutionDTO();
        BeanUtils.copyProperties(execution, dto);
        return dto;
    }

    private Page<WfExecutionDTO> convertToDTOPage(Page<WfTaskExecution> page) {
        Page<WfExecutionDTO> dtoPage = new Page<>();
        dtoPage.setCurrent(page.getCurrent());
        dtoPage.setSize(page.getSize());
        dtoPage.setTotal(page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        return dtoPage;
    }

    private Long requireCurrentUserId() {
        Long userId = CurrentUserUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("未获取到当前登录用户信息");
        }
        return userId;
    }

    private Long requireCurrentTenantId() {
        Long tenantId = CurrentUserUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到当前租户信息");
        }
        return tenantId;
    }

    private String resolveUsername(Long userId) {
        String username = userInfoService.getUsernameById(userId);
        if (StringUtils.hasText(username)) {
            return username;
        }

        String currentAccount = CurrentUserUtils.getAccount();
        if (StringUtils.hasText(currentAccount)) {
            return currentAccount;
        }
        return "用户" + userId;
    }

    private void populateContext(ApprovalContext context,
                                 WfTaskExecution execution,
                                 WfTaskConfig taskConfig,
                                 Long currentNodeId,
                                 String currentNodeName) {
        context.setExecutionId(execution.getId());
        context.setTaskConfigId(execution.getTaskConfigId());
        context.setTaskCode(execution.getTaskCode());
        context.setTaskName(execution.getTaskName());
        context.setCurrentNodeId(currentNodeId);
        context.setCurrentNodeName(currentNodeName);
        context.setInitiatorId(execution.getInitiatorId());
        context.setInitiatorName(execution.getInitiatorName());
        context.setFormContent(execution.getFormContent());
        context.setStatus(execution.getStatus());
        context.setTenantId(execution.getTenantId());
        if (taskConfig != null) {
            context.setTaskConfigId(taskConfig.getId());
        }
    }

    private void callInterpreter(String beanName, InterpreterAction action) {
        IApprovalInterpreter interpreter = interpreterRegistry.getInterpreter(beanName);
        if (interpreter == null) {
            interpreter = interpreterRegistry.getInterpreter("emptyApprovalInterpreter");
        }
        if (interpreter != null) {
            action.execute(interpreter, new ApprovalContext());
        }
    }

    @FunctionalInterface
    private interface InterpreterAction {
        void execute(IApprovalInterpreter interpreter, ApprovalContext context);
    }
}
