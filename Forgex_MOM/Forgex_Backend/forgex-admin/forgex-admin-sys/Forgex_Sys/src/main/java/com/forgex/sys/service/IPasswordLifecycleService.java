package com.forgex.sys.service;

import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.sys.domain.entity.SysUser;

/**
 * 口令生命周期服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IPasswordLifecycleService {

    /**
     * 校验未复用并记录旧口令。
     *
     * @param user        用户
     * @param rawPassword 新明文
     * @param provider    校验器
     * @param policy      策略
     * @return true 表示通过
     */
    boolean assertNotReusedAndRecord(SysUser user, String rawPassword, CryptoPasswordProvider provider, PasswordPolicyConfig policy);
}
