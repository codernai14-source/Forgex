package com.forgex.sys.service;

import com.forgex.common.web.R;

/**
 * 租户忽略配置服务。
 * <p>
 * 提供运行时重新加载忽略规则的能力，用于热更新租户隔离白名单（表/服务/Mapper方法）。
 */
public interface SysTenantIgnoreService {
    /**
     * 重新加载忽略规则。
     * 从数据库读取启用的规则并写入注册表。
     * @return 是否成功
     */
    R<Boolean> reload();
}
