package com.forgex.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.job.domain.entity.SysJobRetry;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.domain.param.JobRetryHandleParam;
import com.forgex.job.service.IJobRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/retry")
@RequiredArgsConstructor
public class JobRetryController {

    private final IJobRetryService jobRetryService;

    @PostMapping("/page")
    @RequirePerm("job:retry:list")
    public R<IPage<SysJobRetry>> page(@RequestBody(required = false) JobPageParam param) {
        return R.ok(jobRetryService.pageRetries(param));
    }

    @PostMapping("/handle")
    @RequirePerm("job:retry:handle")
    public R<Void> handle(@RequestBody JobRetryHandleParam param) {
        jobRetryService.handle(param);
        return R.ok();
    }
}
