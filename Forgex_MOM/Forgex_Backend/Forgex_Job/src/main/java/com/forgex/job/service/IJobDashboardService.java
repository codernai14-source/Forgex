package com.forgex.job.service;

import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.vo.JobDashboardSummaryVO;
import com.forgex.job.domain.vo.JobTrendVO;

import java.util.List;

/**
 * Job 大盘服务。
 *
 * @author Forgex
 * @version 1.0.0
 */
public interface IJobDashboardService {
    JobDashboardSummaryVO summary();
    List<JobTrendVO> trend();
    List<SysJobLog> recentFailures();
    List<SysJobLog> top();
}
