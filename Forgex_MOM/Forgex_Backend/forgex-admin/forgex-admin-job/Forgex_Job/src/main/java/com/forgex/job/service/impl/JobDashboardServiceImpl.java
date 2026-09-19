package com.forgex.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.domain.vo.JobDashboardSummaryVO;
import com.forgex.job.domain.vo.JobTrendVO;
import com.forgex.job.enums.JobConstants;
import com.forgex.job.service.IJobDashboardService;
import com.forgex.job.service.IJobInstanceService;
import com.forgex.job.service.IJobLogService;
import com.forgex.job.service.IJobTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@DS("job")
public class JobDashboardServiceImpl implements IJobDashboardService {
    private final IJobTaskService taskService;
    private final IJobLogService logService;
    private final IJobInstanceService instanceService;

    public JobDashboardServiceImpl(IJobTaskService taskService, IJobLogService logService, IJobInstanceService instanceService) {
        this.taskService = taskService;
        this.logService = logService;
        this.instanceService = instanceService;
    }

    @Override
    public JobDashboardSummaryVO summary() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        long todayTotal = logService.count(new LambdaQueryWrapper<SysJobLog>().ge(SysJobLog::getStartTime, today));
        long success = logService.count(new LambdaQueryWrapper<SysJobLog>().ge(SysJobLog::getStartTime, today).eq(SysJobLog::getStatus, JobConstants.LOG_SUCCESS));
        long failed = logService.count(new LambdaQueryWrapper<SysJobLog>().ge(SysJobLog::getStartTime, today).eq(SysJobLog::getStatus, JobConstants.LOG_FAILED));
        long timeout = logService.count(new LambdaQueryWrapper<SysJobLog>().ge(SysJobLog::getStartTime, today).eq(SysJobLog::getStatus, JobConstants.LOG_TIMEOUT));
        JobDashboardSummaryVO vo = new JobDashboardSummaryVO();
        vo.setTotalTasks(taskService.count());
        vo.setEnabledTasks(taskService.count(new LambdaQueryWrapper<SysJobTask>().eq(SysJobTask::getStatus, JobConstants.STATUS_ENABLED)));
        vo.setTodayExecutions(todayTotal);
        vo.setSuccessExecutions(success);
        vo.setFailedExecutions(failed);
        vo.setTimeoutExecutions(timeout);
        vo.setOnlineInstances(instanceService.countOnline());
        vo.setSuccessRate(todayTotal == 0 ? 0D : success * 100D / todayTotal);
        return vo;
    }

    @Override
    public List<JobTrendVO> trend() {
        List<JobTrendVO> list = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now().minusHours(23).withMinute(0).withSecond(0).withNano(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:00");
        for (int i = 0; i < 24; i++) {
            LocalDateTime begin = start.plusHours(i);
            LocalDateTime end = begin.plusHours(1);
            long success = countByStatus(begin, end, JobConstants.LOG_SUCCESS);
            long failed = countByStatus(begin, end, JobConstants.LOG_FAILED);
            long timeout = countByStatus(begin, end, JobConstants.LOG_TIMEOUT);
            list.add(new JobTrendVO(begin.format(formatter), success, failed, timeout));
        }
        return list;
    }

    @Override
    public List<SysJobLog> recentFailures() {
        return logService.list(new LambdaQueryWrapper<SysJobLog>()
            .in(SysJobLog::getStatus, JobConstants.LOG_FAILED, JobConstants.LOG_TIMEOUT)
            .orderByDesc(SysJobLog::getStartTime)
            .last("limit 10"));
    }

    @Override
    public List<SysJobLog> top() {
        return logService.list(new LambdaQueryWrapper<SysJobLog>()
            .orderByDesc(SysJobLog::getDurationMs)
            .last("limit 10"));
    }

    private long countByStatus(LocalDateTime begin, LocalDateTime end, int status) {
        return logService.count(new LambdaQueryWrapper<SysJobLog>()
            .ge(SysJobLog::getStartTime, begin)
            .lt(SysJobLog::getStartTime, end)
            .eq(SysJobLog::getStatus, status));
    }
}
