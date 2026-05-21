package com.forgex.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.job.core.cron.JobTriggerTimeCalculator;
import com.forgex.job.core.executor.JobExecutor;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobStatusParam;
import com.forgex.job.domain.param.JobTaskSaveParam;
import com.forgex.job.domain.param.JobTriggerParam;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.enums.JobPromptEnum;
import com.forgex.job.mapper.SysJobTaskMapper;
import com.forgex.job.service.IJobTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Job 任务服务实现。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Service
@DS("job")
public class JobTaskServiceImpl extends ServiceImpl<SysJobTaskMapper, SysJobTask> implements IJobTaskService {

    private final JobTriggerTimeCalculator calculator;
    private final ObjectMapper objectMapper;
    private final JobExecutor jobExecutor;

    public JobTaskServiceImpl(JobTriggerTimeCalculator calculator, ObjectMapper objectMapper, JobExecutor jobExecutor) {
        this.calculator = calculator;
        this.objectMapper = objectMapper;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public IPage<SysJobTask> pageTasks(JobPageParam param) {
        JobPageParam query = param == null ? new JobPageParam() : param;
        LambdaQueryWrapper<SysJobTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getJobCode()), SysJobTask::getJobCode, query.getJobCode());
        wrapper.like(StringUtils.hasText(query.getJobName()), SysJobTask::getJobName, query.getJobName());
        wrapper.like(StringUtils.hasText(query.getJobGroup()), SysJobTask::getJobGroup, query.getJobGroup());
        wrapper.eq(query.getJobType() != null, SysJobTask::getJobType, query.getJobType());
        wrapper.eq(query.getScheduleType() != null, SysJobTask::getScheduleType, query.getScheduleType());
        wrapper.eq(query.getStatus() != null, SysJobTask::getStatus, query.getStatus());
        wrapper.orderByDesc(SysJobTask::getId);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTask(JobTaskSaveParam param) {
        validate(param);
        SysJobTask entity = param.getId() == null ? new SysJobTask() : detail(param.getId());
        if (param.getId() != null && !entity.getJobCode().equals(param.getJobCode())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_CODE_IMMUTABLE);
        }
        BeanUtils.copyProperties(param, entity);
        if (entity.getJobType() == null) {
            entity.setJobType(JobConstants.JOB_TYPE_JAVA_BEAN);
        }
        if (entity.getScheduleType() == null) {
            entity.setScheduleType(JobConstants.SCHEDULE_MANUAL);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(JobConstants.STATUS_DISABLED);
        }
        if (entity.getBlockStrategy() == null) {
            entity.setBlockStrategy(JobConstants.BLOCK_DISCARD);
        }
        if (entity.getShardTotal() == null || entity.getShardTotal() <= 0) {
            entity.setShardTotal(1);
        }
        entity.setNextTriggerTime(calculator.next(entity, LocalDateTime.now()));
        saveOrUpdate(entity);
        return entity.getId();
    }

    @Override
    public SysJobTask detail(Long id) {
        SysJobTask task = getById(id);
        if (task == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_NOT_FOUND);
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        detail(id);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(JobStatusParam param) {
        SysJobTask task = detail(param.getId());
        task.setStatus(param.getStatus());
        if (JobConstants.STATUS_ENABLED == param.getStatus()) {
            task.setNextTriggerTime(calculator.next(task, LocalDateTime.now()));
        }
        updateById(task);
    }

    @Override
    public Long trigger(JobTriggerParam param) {
        SysJobTask task = detail(param.getId());
        String params = StringUtils.hasText(param.getParams()) ? param.getParams() : task.getJobParams();
        validateJson(params);
        return jobExecutor.submit(task, JobConstants.TRIGGER_MANUAL, params, param.getRequestId());
    }

    @Override
    public List<LocalDateTime> previewTriggerTimes(JobTriggerParam param) {
        SysJobTask task = detail(param.getId());
        List<LocalDateTime> times = new ArrayList<>();
        LocalDateTime cursor = LocalDateTime.now();
        for (int i = 0; i < 5; i++) {
            cursor = calculator.next(task, cursor);
            if (cursor == null) {
                break;
            }
            times.add(cursor);
        }
        return times;
    }

    private void validate(JobTaskSaveParam param) {
        if (param == null || !StringUtils.hasText(param.getJobCode()) || !StringUtils.hasText(param.getJobName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_PARAM_REQUIRED);
        }
        Long count = count(new LambdaQueryWrapper<SysJobTask>()
            .eq(SysJobTask::getJobCode, param.getJobCode())
            .ne(param.getId() != null, SysJobTask::getId, param.getId()));
        if (count != null && count > 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_CODE_EXISTS);
        }
        if (JobConstants.SCHEDULE_CRON == safeInt(param.getScheduleType())) {
            try {
                calculator.validateCron(param.getCronExpression());
            } catch (Exception ex) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_CRON_INVALID);
            }
        }
        validateJson(param.getJobParams());
        validateJson(param.getHttpHeaders());
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void validateJson(String json) {
        if (!StringUtils.hasText(json)) {
            return;
        }
        try {
            objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, JobPromptEnum.JOB_JSON_INVALID);
        }
    }
}
