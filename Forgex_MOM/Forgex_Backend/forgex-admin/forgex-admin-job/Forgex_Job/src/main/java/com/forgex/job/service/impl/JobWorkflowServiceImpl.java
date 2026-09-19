package com.forgex.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.job.domain.entity.SysJobWorkflow;
import com.forgex.job.domain.entity.SysJobWorkflowExecution;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobWorkflowSaveParam;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.enums.JobPromptEnum;
import com.forgex.job.mapper.SysJobWorkflowExecutionMapper;
import com.forgex.job.mapper.SysJobWorkflowMapper;
import com.forgex.job.service.IJobWorkflowService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * DAG 工作流服务实现。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Service
@DS("job")
public class JobWorkflowServiceImpl extends ServiceImpl<SysJobWorkflowMapper, SysJobWorkflow> implements IJobWorkflowService {

    private final SysJobWorkflowExecutionMapper executionMapper;
    private final ObjectMapper objectMapper;

    public JobWorkflowServiceImpl(SysJobWorkflowExecutionMapper executionMapper, ObjectMapper objectMapper) {
        this.executionMapper = executionMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public IPage<SysJobWorkflow> pageWorkflows(JobPageParam param) {
        JobPageParam query = param == null ? new JobPageParam() : param;
        LambdaQueryWrapper<SysJobWorkflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getWorkflowCode()), SysJobWorkflow::getWorkflowCode, query.getWorkflowCode());
        wrapper.like(StringUtils.hasText(query.getWorkflowName()), SysJobWorkflow::getWorkflowName, query.getWorkflowName());
        wrapper.eq(query.getStatus() != null, SysJobWorkflow::getStatus, query.getStatus());
        wrapper.orderByDesc(SysJobWorkflow::getId);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveWorkflow(JobWorkflowSaveParam param) {
        validateGraph(param.getGraphJson());
        SysJobWorkflow entity = param.getId() == null ? new SysJobWorkflow() : getById(param.getId());
        if (entity == null) {
            entity = new SysJobWorkflow();
        }
        BeanUtils.copyProperties(param, entity);
        if (entity.getStatus() == null) {
            entity.setStatus(JobConstants.WORKFLOW_DRAFT);
        }
        saveOrUpdate(entity);
        return entity.getId();
    }

    @Override
    public void publish(Long id) {
        SysJobWorkflow workflow = getById(id);
        if (workflow == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_WORKFLOW_NOT_FOUND);
        }
        validateGraph(workflow.getGraphJson());
        workflow.setStatus(JobConstants.WORKFLOW_PUBLISHED);
        updateById(workflow);
    }

    @Override
    public Long execute(Long id) {
        SysJobWorkflow workflow = getById(id);
        if (workflow == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_WORKFLOW_NOT_FOUND);
        }
        SysJobWorkflowExecution execution = new SysJobWorkflowExecution();
        execution.setTenantId(workflow.getTenantId());
        execution.setWorkflowId(workflow.getId());
        execution.setWorkflowCode(workflow.getWorkflowCode());
        execution.setStatus(JobConstants.LOG_SUCCESS);
        execution.setStartTime(LocalDateTime.now());
        execution.setEndTime(LocalDateTime.now());
        execution.setNodeStatusJson("{}");
        execution.setResultMessage("workflow accepted");
        executionMapper.insert(execution);
        return execution.getId();
    }

    @Override
    public IPage<SysJobWorkflowExecution> pageExecutions(JobPageParam param) {
        JobPageParam query = param == null ? new JobPageParam() : param;
        LambdaQueryWrapper<SysJobWorkflowExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getWorkflowId() != null, SysJobWorkflowExecution::getWorkflowId, query.getWorkflowId());
        wrapper.like(StringUtils.hasText(query.getWorkflowCode()), SysJobWorkflowExecution::getWorkflowCode, query.getWorkflowCode());
        wrapper.orderByDesc(SysJobWorkflowExecution::getId);
        return executionMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    private void validateGraph(String graphJson) {
        if (!StringUtils.hasText(graphJson)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(graphJson);
            JsonNode edges = root.get("edges");
            if (edges == null || !edges.isArray()) {
                return;
            }
            Map<String, List<String>> graph = new HashMap<>();
            for (JsonNode edge : edges) {
                String source = edge.path("source").asText("");
                String target = edge.path("target").asText("");
                if (StringUtils.hasText(source) && StringUtils.hasText(target)) {
                    graph.computeIfAbsent(source, key -> new ArrayList<>()).add(target);
                }
            }
            if (hasCycle(graph)) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_WORKFLOW_CYCLE);
            }
        } catch (I18nBusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_JSON_INVALID);
        }
    }

    private boolean hasCycle(Map<String, List<String>> graph) {
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String node : graph.keySet()) {
            if (dfs(node, graph, visiting, visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfs(String node, Map<String, List<String>> graph, Set<String> visiting, Set<String> visited) {
        if (visited.contains(node)) {
            return false;
        }
        if (!visiting.add(node)) {
            return true;
        }
        for (String next : graph.getOrDefault(node, Collections.emptyList())) {
            if (dfs(next, graph, visiting, visited)) {
                return true;
            }
        }
        visiting.remove(node);
        visited.add(node);
        return false;
    }
}
