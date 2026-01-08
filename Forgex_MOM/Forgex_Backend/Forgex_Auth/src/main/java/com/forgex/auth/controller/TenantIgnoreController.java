package com.forgex.auth.controller;

import com.forgex.auth.service.TenantIgnoreService;
import com.forgex.common.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户隔离跳过配置控制器
 * 提供运行时热更新接口
 */
@RestController
@RequestMapping("/tenant/ignore")
public class TenantIgnoreController {
    @Autowired
    private TenantIgnoreService service;

    /**
     * 热更新跳过配置
     * @return 是否成功
     */
    @PostMapping("/reload")
    public R<Boolean> reload() {
        return service.reload();
    }
}

