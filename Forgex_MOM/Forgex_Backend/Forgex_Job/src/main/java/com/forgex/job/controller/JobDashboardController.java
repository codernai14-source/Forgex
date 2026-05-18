package com.forgex.job.controller;

import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.vo.JobDashboardSummaryVO;
import com.forgex.job.domain.vo.JobTrendVO;
import com.forgex.job.service.IJobDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@RequirePerm("job:dashboard:view")
public class JobDashboardController {

    private final IJobDashboardService jobDashboardService;

    @GetMapping("/summary")
    public R<JobDashboardSummaryVO> summary() {
        return R.ok(jobDashboardService.summary());
    }

    @GetMapping("/trend")
    public R<List<JobTrendVO>> trend() {
        return R.ok(jobDashboardService.trend());
    }

    @GetMapping("/recent-failures")
    public R<List<SysJobLog>> recentFailures() {
        return R.ok(jobDashboardService.recentFailures());
    }

    @GetMapping("/top")
    public R<List<SysJobLog>> top() {
        return R.ok(jobDashboardService.top());
    }
}
