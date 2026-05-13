package com.forgex.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.job.domain.entity.SysJobInstance;
import com.forgex.job.domain.param.JobInstanceMaintainParam;
import com.forgex.job.domain.param.JobPageParam;

/**
 * Job 实例服务。
 *
 * @author Forgex
 * @version 1.0.0
 */
public interface IJobInstanceService extends IService<SysJobInstance> {
    IPage<SysJobInstance> pageInstances(JobPageParam param);
    String currentInstanceId();
    void registerOrHeartbeat();
    void changeMaintenance(JobInstanceMaintainParam param);
    long countOnline();
    void increaseRunning();
    void decreaseRunning();
}
