package com.forgex.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.job.domain.entity.SysJobWorkflow;
import com.forgex.job.domain.entity.SysJobWorkflowExecution;
import com.forgex.job.domain.param.IdParam;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobWorkflowSaveParam;
import com.forgex.job.service.IJobWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class JobWorkflowController {

    private final IJobWorkflowService jobWorkflowService;

    @PostMapping("/page")
    @RequirePerm("job:workflow:list")
    public R<IPage<SysJobWorkflow>> page(@RequestBody(required = false) JobPageParam param) {
        return R.ok(jobWorkflowService.pageWorkflows(param));
    }

    @PostMapping("/detail")
    @RequirePerm("job:workflow:view")
    public R<SysJobWorkflow> detail(@RequestBody IdParam param) {
        return R.ok(jobWorkflowService.getById(param.getId()));
    }

    @PostMapping("/save")
    @RequirePerm({"job:workflow:add", "job:workflow:edit"})
    public R<Long> save(@RequestBody JobWorkflowSaveParam param) {
        return R.ok(jobWorkflowService.saveWorkflow(param));
    }

    @PostMapping("/publish")
    @RequirePerm("job:workflow:publish")
    public R<Void> publish(@RequestBody IdParam param) {
        jobWorkflowService.publish(param.getId());
        return R.ok();
    }

    @PostMapping("/execute")
    @RequirePerm("job:workflow:execute")
    public R<Long> execute(@RequestBody IdParam param) {
        return R.ok(jobWorkflowService.execute(param.getId()));
    }

    @PostMapping("/execution-page")
    @RequirePerm("job:workflow:view")
    public R<IPage<SysJobWorkflowExecution>> executionPage(@RequestBody(required = false) JobPageParam param) {
        return R.ok(jobWorkflowService.pageExecutions(param));
    }
}
