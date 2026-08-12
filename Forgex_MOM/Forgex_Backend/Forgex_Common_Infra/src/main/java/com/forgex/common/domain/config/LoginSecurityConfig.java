package com.forgex.common.domain.config;

import lombok.Data;

/**
 * 登录失败安全配置
 * <p>
 * 用于配置登录失败的安全策略，包括连续失败次数统计、锁定机制等。
 * 该配置用于防止暴力破解密码，保护账户安全。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 */
@Data
public class LoginSecurityConfig {
    
    /**
     * 连续登录失败统计时长
     * <p>统计连续登录失败次数的时间窗口，单位为分钟。</p>
     */
    private Integer failWindowMinutes;
    
    /**
     * 连续登录失败次数阈值
     * <p>触发账户锁定的最大失败次数。</p>
     */
    private Integer maxFailCount;
    
    /**
     * 锁定时长
     * <p>账户被锁定后禁止登录的时长，单位为分钟。</p>
     */
    private Integer lockMinutes;
    
    /**
     * 创建默认的登录安全配置
     *
     * @return 默认的登录安全配置对象
     */
    public static LoginSecurityConfig defaults() {
        LoginSecurityConfig config = new LoginSecurityConfig();
        config.setFailWindowMinutes(15);
        config.setMaxFailCount(5);
        config.setLockMinutes(30);
        return config;
    }
}
