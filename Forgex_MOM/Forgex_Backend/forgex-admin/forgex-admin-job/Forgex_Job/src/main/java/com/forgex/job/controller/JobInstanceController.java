package com.forgex.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.job.domain.entity.SysJobInstance;
import com.forgex.job.domain.param.JobInstanceMaintainParam;
import com.forgex.job.domain.param.JobPageParam;
import com.forgex.job.service.IJobInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/instance")
@RequiredArgsConstructor
public class JobInstanceController {

    private final IJobInstanceService jobInstanceService;

    @PostMapping("/page")
    @RequirePerm("job:instance:list")
    public R<IPage<SysJobInstance>> page(@RequestBody(required = false) JobPageParam param) {
        return R.ok(jobInstanceService.pageInstances(param));
    }

    @PostMapping("/change-maintenance")
    @RequirePerm("job:instance:maintenance")
    public R<Void> changeMaintenance(@RequestBody JobInstanceMaintainParam param) {
        jobInstanceService.changeMaintenance(param);
        return R.ok();
    }
}
