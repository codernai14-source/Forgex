package com.forgex.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobStatusParam;
import com.forgex.job.domain.param.JobTaskSaveParam;
import com.forgex.job.domain.param.JobTriggerParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job 任务服务。
 *
 * @author Forgex
 * @version 1.0.0
 */
public interface IJobTaskService extends IService<SysJobTask> {
    IPage<SysJobTask> pageTasks(JobPageParam param);
    Long saveTask(JobTaskSaveParam param);
    SysJobTask detail(Long id);
    void deleteTask(Long id);
    void changeStatus(JobStatusParam param);
    Long trigger(JobTriggerParam param);
    List<LocalDateTime> previewTriggerTimes(JobTriggerParam param);
}
