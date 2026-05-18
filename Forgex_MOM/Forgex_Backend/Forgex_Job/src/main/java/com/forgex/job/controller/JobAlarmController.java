package com.forgex.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.job.domain.entity.SysJobAlarmRule;
import com.forgex.job.domain.param.IdParam;
import com.forgex.job.domain.param.JobAlarmRuleSaveParam;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.service.IJobAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class JobAlarmController {

    private final IJobAlarmService jobAlarmService;

    @PostMapping("/page")
    @RequirePerm("job:alarm:list")
    public R<IPage<SysJobAlarmRule>> page(@RequestBody(required = false) JobPageParam param) {
        return R.ok(jobAlarmService.pageRules(param));
    }

    @PostMapping("/detail")
    @RequirePerm("job:alarm:view")
    public R<SysJobAlarmRule> detail(@RequestBody IdParam param) {
        return R.ok(jobAlarmService.getById(param.getId()));
    }

    @PostMapping("/save")
    @RequirePerm({"job:alarm:add", "job:alarm:edit"})
    public R<Long> save(@RequestBody JobAlarmRuleSaveParam param) {
        return R.ok(jobAlarmService.saveRule(param));
    }

    @PostMapping("/delete")
    @RequirePerm("job:alarm:delete")
    public R<Void> delete(@RequestBody IdParam param) {
        jobAlarmService.deleteRule(param.getId());
        return R.ok();
    }
}
