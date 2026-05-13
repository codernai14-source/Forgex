package com.forgex.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.job.domain.entity.SysJobRetry;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobRetryHandleParam;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.mapper.SysJobRetryMapper;
import com.forgex.job.service.IJobRetryService;
import org.springframework.stereotype.Service;

@Service
@DS("job")
public class JobRetryServiceImpl extends ServiceImpl<SysJobRetryMapper, SysJobRetry> implements IJobRetryService {
    @Override
    public IPage<SysJobRetry> pageRetries(JobPageParam param) {
        JobPageParam query = param == null ? new JobPageParam() : param;
        LambdaQueryWrapper<SysJobRetry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getJobId() != null, SysJobRetry::getJobId, query.getJobId());
        wrapper.like(query.getJobCode() != null && !query.getJobCode().isBlank(), SysJobRetry::getJobCode, query.getJobCode());
        wrapper.eq(query.getStatus() != null, SysJobRetry::getStatus, query.getStatus());
        wrapper.orderByDesc(SysJobRetry::getNextRetryTime);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public void handle(JobRetryHandleParam param) {
        SysJobRetry retry = getById(param.getId());
        if (retry == null) {
            return;
        }
        retry.setStatus(param.getAction() != null && param.getAction() == 1
            ? JobConstants.RETRY_WAITING : JobConstants.RETRY_IGNORED);
        retry.setHandleRemark(param.getRemark());
        updateById(retry);
    }
}
