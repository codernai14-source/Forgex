package com.forgex.auth.service;

import com.forgex.common.web.R;

/**
 * 租户隔离跳过配置服务
 * 提供运行时热更新能力
 */
public interface TenantIgnoreService {
    /**
     * 重新加载跳过配置
     * @return 是否成功
     */
    R<Boolean> reload();
}

