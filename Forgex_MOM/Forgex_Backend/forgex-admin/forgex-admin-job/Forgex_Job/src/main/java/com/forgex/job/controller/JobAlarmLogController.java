package com.forgex.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.job.domain.entity.SysJobAlarmLog;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.service.IJobAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alarm-log")
@RequiredArgsConstructor
public class JobAlarmLogController {

    private final IJobAlarmService jobAlarmService;

    @PostMapping("/page")
    @RequirePerm("job:alarm:list")
    public R<IPage<SysJobAlarmLog>> page(@RequestBody(required = false) JobPageParam param) {
        return R.ok(jobAlarmService.pageLogs(param));
    }
}
