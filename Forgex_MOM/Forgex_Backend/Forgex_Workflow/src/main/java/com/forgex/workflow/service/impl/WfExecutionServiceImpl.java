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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.exception.BusinessException;
import com.forgex.workflow.domain.dto.WfExecutionDTO;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskExecutionDetail;
import com.forgex.workflow.domain.param.WfExecutionApproveParam;
import com.forgex.workflow.domain.param.WfExecutionQueryParam;
import com.forgex.workflow.domain.param.WfExecutionStartParam;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionDetailMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.service.IWfEngineService;
import com.forgex.workflow.service.IWfExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批执行 Service 实现。
 * <p>
 * 提供审批执行的业务逻辑实现。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfExecutionServiceImpl implements IWfExecutionService {
    
    private final WfTaskExecutionMapper executionMapper;
    private final WfTaskExecutionDetailMapper executionDetailMapper;
    private final WfTaskConfigMapper taskConfigMapper;
    private final IWfEngineService engineService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startExecution(WfExecutionStartParam param) {
        // 获取任务配置
        WfTaskConfig taskConfig = taskConfigMapper.selectOne(new LambdaQueryWrapper<WfTaskConfig>()
            .eq(WfTaskConfig::getTaskCode, param.getTaskCode()));
        if (taskConfig == null) {
            throw new BusinessException("审批任务配置不存在");
        }
        
        if (taskConfig.getStatus() == 0) {
            throw new BusinessException("审批任务已禁用");
        }
        
        // 创建执行记录
        WfTaskExecution execution = new WfTaskExecution();
        execution.setTaskConfigId(taskConfig.getId());
        execution.setTaskCode(taskConfig.getTaskCode());
        execution.setTaskName(taskConfig.getTaskName());
        execution.setFormContent(param.getFormContent());
        execution.setStatus(1); // 审批中
        execution.setStartTime(LocalDateTime.now());
        
        // TODO: 从上下文获取当前用户信息
        execution.setInitiatorId(1L);
        execution.setInitiatorName("当前用户");
        
        executionMapper.insert(execution);
        
        // 初始化审批流程
        engineService.initExecution(execution.getId(), taskConfig.getId());
        
        log.info("发起审批成功，执行 ID：{}，任务名称：{}", execution.getId(), taskConfig.getTaskName());
        
        return execution.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(WfExecutionApproveParam param) {
        // 获取执行记录
        WfTaskExecution execution = executionMapper.selectById(param.getExecutionId());
        if (execution == null) {
            throw new BusinessException("审批执行记录不存在");
        }
        
        if (execution.getStatus() != 1) {
            throw new BusinessException("审批任务不在审批中状态");
        }
        
        // 获取任务配置
        WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
        
        // 更新审批详情
        // TODO: 记录审批人信息
        
        // 流转到下一节点
        engineService.moveToNextNode(param.getExecutionId(), execution.getCurrentNodeId(), 1, null);
        
        // 调用解释器
        // TODO: 调用 onApprove
        
        log.info("审批同意成功，执行 ID：{}", param.getExecutionId());
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(WfExecutionApproveParam param) {
        // 获取执行记录
        WfTaskExecution execution = executionMapper.selectById(param.getExecutionId());
        if (execution == null) {
            throw new BusinessException("审批执行记录不存在");
        }
        
        if (execution.getStatus() != 1) {
            throw new BusinessException("审批任务不在审批中状态");
        }
        
        // 更新审批详情
        // TODO: 记录审批人信息和驳回原因
        
        // 流转到下一节点（驳回）
        engineService.moveToNextNode(param.getExecutionId(), execution.getCurrentNodeId(), 2, param.getRejectType());
        
        log.info("审批驳回成功，执行 ID：{}", param.getExecutionId());
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelExecution(Long executionId) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("审批执行记录不存在");
        }
        
        if (execution.getStatus() != 1) {
            throw new BusinessException("审批任务不在审批中状态，无法撤销");
        }
        
        // TODO: 检查是否是发起人本人撤销
        
        // 更新状态为驳回
        execution.setStatus(3);
        execution.setEndTime(LocalDateTime.now());
        executionMapper.updateById(execution);
        
        log.info("撤销审批成功，执行 ID：{}", executionId);
        
        return true;
    }
    
    @Override
    public WfExecutionDTO getExecutionDetail(Long executionId) {
        WfTaskExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            return null;
        }
        
        return convertToDTO(execution);
    }
    
    @Override
    public Page<WfExecutionDTO> pageMyInitiated(WfExecutionQueryParam param) {
        Page<WfTaskExecution> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<WfTaskExecution> wrapper = buildQueryWrapper(param);
        wrapper.eq(WfTaskExecution::getInitiatorId, getCurrentUserId());
        wrapper.orderByDesc(WfTaskExecution::getCreateTime);
        
        Page<WfTaskExecution> executionPage = executionMapper.selectPage(page, wrapper);
        
        return convertToDTOPage(executionPage);
    }
    
    @Override
    public Page<WfExecutionDTO> pageMyPending(WfExecutionQueryParam param) {
        // TODO: 查询我待处理的审批
        // 需要关联 wf_my_task 表
        return new Page<>();
    }
    
    @Override
    public Page<WfExecutionDTO> pageMyProcessed(WfExecutionQueryParam param) {
        // TODO: 查询我已处理的审批
        // 需要关联 wf_task_execution_approver 表
        return new Page<>();
    }
    
    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<WfTaskExecution> buildQueryWrapper(WfExecutionQueryParam param) {
        LambdaQueryWrapper<WfTaskExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskExecution::getDeleted, false);
        
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
    
    /**
     * 实体转 DTO
     */
    private WfExecutionDTO convertToDTO(WfTaskExecution execution) {
        WfExecutionDTO dto = new WfExecutionDTO();
        BeanUtils.copyProperties(execution, dto);
        return dto;
    }
    
    /**
     * 分页实体转 DTO
     */
    private Page<WfExecutionDTO> convertToDTOPage(Page<WfTaskExecution> page) {
        Page<WfExecutionDTO> dtoPage = new Page<>();
        dtoPage.setCurrent(page.getCurrent());
        dtoPage.setSize(page.getSize());
        dtoPage.setTotal(page.getTotal());
        
        List<WfExecutionDTO> records = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(records);
        
        return dtoPage;
    }
    
    /**
     * 获取当前用户 ID
     */
    private Long getCurrentUserId() {
        // TODO: 从 Sa-Token 上下文获取当前登录用户 ID
        return 1L;
    }
}