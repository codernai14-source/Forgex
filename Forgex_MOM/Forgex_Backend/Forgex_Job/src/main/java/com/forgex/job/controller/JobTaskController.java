package com.forgex.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.job.domain.entity.SysJobTask;
import com.forgex.job.domain.param.IdParam;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobStatusParam;
import com.forgex.job.domain.param.JobTaskSaveParam;
import com.forgex.job.domain.param.JobTriggerParam;
import com.forgex.job.service.IJobTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class JobTaskController {

    private final IJobTaskService jobTaskService;

    @PostMapping("/page")
    @RequirePerm("job:task:list")
    public R<IPage<SysJobTask>> page(@RequestBody(required = false) JobPageParam param) {
        return R.ok(jobTaskService.pageTasks(param));
    }

    @PostMapping("/detail")
    @RequirePerm("job:task:view")
    public R<SysJobTask> detail(@RequestBody IdParam param) {
        return R.ok(jobTaskService.detail(param.getId()));
    }

    @PostMapping("/save")
    @RequirePerm({"job:task:add", "job:task:edit"})
    public R<Long> save(@RequestBody JobTaskSaveParam param) {
        return R.ok(jobTaskService.saveTask(param));
    }

    @PostMapping("/delete")
    @RequirePerm("job:task:delete")
    public R<Void> delete(@RequestBody IdParam param) {
        jobTaskService.deleteTask(param.getId());
        return R.ok();
    }

    @PostMapping("/change-status")
    @RequirePerm("job:task:changeStatus")
    public R<Void> changeStatus(@RequestBody JobStatusParam param) {
        jobTaskService.changeStatus(param);
        return R.ok();
    }

    @PostMapping("/trigger")
    @RequirePerm("job:task:trigger")
    public R<Long> trigger(@RequestBody JobTriggerParam param) {
        return R.ok(jobTaskService.trigger(param));
    }

    @PostMapping("/preview-trigger-times")
    @RequirePerm("job:task:view")
    public R<List<LocalDateTime>> previewTriggerTimes(@RequestBody JobTriggerParam param) {
        return R.ok(jobTaskService.previewTriggerTimes(param));
    }
}
