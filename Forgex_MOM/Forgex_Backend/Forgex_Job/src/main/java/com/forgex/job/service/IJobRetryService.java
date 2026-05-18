package com.forgex.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.job.domain.entity.SysJobRetry;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobRetryHandleParam;

public interface IJobRetryService extends IService<SysJobRetry> {
    IPage<SysJobRetry> pageRetries(JobPageParam param);
    void handle(JobRetryHandleParam param);
}
