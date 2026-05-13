package com.forgex.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.job.domain.entity.SysJobAlarmLog;
import com.forgex.job.domain.entity.SysJobAlarmRule;
import com.forgex.job.domain.param.JobAlarmRuleSaveParam;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.mapper.SysJobAlarmLogMapper;
import com.forgex.job.mapper.SysJobAlarmRuleMapper;
import com.forgex.job.service.IJobAlarmService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@DS("job")
public class JobAlarmServiceImpl extends ServiceImpl<SysJobAlarmRuleMapper, SysJobAlarmRule> implements IJobAlarmService {
    private final SysJobAlarmLogMapper logMapper;

    public JobAlarmServiceImpl(SysJobAlarmLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public IPage<SysJobAlarmRule> pageRules(JobPageParam param) {
        JobPageParam query = param == null ? new JobPageParam() : param;
        LambdaQueryWrapper<SysJobAlarmRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getRuleName()), SysJobAlarmRule::getRuleName, query.getRuleName());
        wrapper.like(StringUtils.hasText(query.getJobCode()), SysJobAlarmRule::getJobCode, query.getJobCode());
        wrapper.eq(query.getStatus() != null, SysJobAlarmRule::getStatus, query.getStatus());
        wrapper.orderByDesc(SysJobAlarmRule::getId);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveRule(JobAlarmRuleSaveParam param) {
        SysJobAlarmRule entity = param.getId() == null ? new SysJobAlarmRule() : getById(param.getId());
        if (entity == null) {
            entity = new SysJobAlarmRule();
        }
        BeanUtils.copyProperties(param, entity);
        saveOrUpdate(entity);
        return entity.getId();
    }

    @Override
    public void deleteRule(Long id) {
        removeById(id);
    }

    @Override
    public IPage<SysJobAlarmLog> pageLogs(JobPageParam param) {
        JobPageParam query = param == null ? new JobPageParam() : param;
        LambdaQueryWrapper<SysJobAlarmLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getJobId() != null, SysJobAlarmLog::getJobId, query.getJobId());
        wrapper.like(StringUtils.hasText(query.getJobCode()), SysJobAlarmLog::getJobCode, query.getJobCode());
        wrapper.eq(query.getStatus() != null, SysJobAlarmLog::getSendStatus, query.getStatus());
        wrapper.orderByDesc(SysJobAlarmLog::getId);
        return logMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }
}
