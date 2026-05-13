package com.forgex.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.job.domain.entity.SysJobWorkflow;
import com.forgex.job.domain.entity.SysJobWorkflowExecution;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobWorkflowSaveParam;

public interface IJobWorkflowService extends IService<SysJobWorkflow> {
    IPage<SysJobWorkflow> pageWorkflows(JobPageParam param);
    Long saveWorkflow(JobWorkflowSaveParam param);
    void publish(Long id);
    Long execute(Long id);
    IPage<SysJobWorkflowExecution> pageExecutions(JobPageParam param);
}
