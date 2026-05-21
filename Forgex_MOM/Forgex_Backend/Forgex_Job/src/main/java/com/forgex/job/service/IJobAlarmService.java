package com.forgex.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.job.domain.entity.SysJobAlarmLog;
import com.forgex.job.domain.entity.SysJobAlarmRule;
import com.forgex.job.domain.param.JobAlarmRuleSaveParam;
import com.forgex.job.domain.param.JobPageParam;

public interface IJobAlarmService extends IService<SysJobAlarmRule> {
    IPage<SysJobAlarmRule> pageRules(JobPageParam param);
    Long saveRule(JobAlarmRuleSaveParam param);
    void deleteRule(Long id);
    IPage<SysJobAlarmLog> pageLogs(JobPageParam param);
}
