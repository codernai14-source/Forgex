package com.forgex.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.job.domain.entity.SysJobLog;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.service.IJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class JobLogController {

    private final IJobLogService jobLogService;

    @PostMapping("/page")
    @RequirePerm("job:log:list")
    public R<IPage<SysJobLog>> page(@RequestBody(required = false) JobPageParam param) {
        return R.ok(jobLogService.pageLogs(param));
    }

    @GetMapping("/detail/{id}")
    @RequirePerm("job:log:view")
    public R<SysJobLog> detail(@PathVariable Long id) {
        return R.ok(jobLogService.getById(id));
    }
}
