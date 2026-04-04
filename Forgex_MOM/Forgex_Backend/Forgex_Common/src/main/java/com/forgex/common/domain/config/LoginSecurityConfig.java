package com.forgex.common.domain.config;

import lombok.Data;

/**
 * 登录失败安全配置。
 */
@Data
public class LoginSecurityConfig {

    /** 连续登录失败统计时长，单位：分钟 */
    private Integer failWindowMinutes;

    /** 连续登录失败次数阈值 */
    private Integer maxFailCount;

    /** 锁定时长，单位：分钟 */
    private Integer lockMinutes;

    public static LoginSecurityConfig defaults() {
        LoginSecurityConfig config = new LoginSecurityConfig();
        config.setFailWindowMinutes(15);
        config.setMaxFailCount(5);
        config.setLockMinutes(30);
        return config;
    }
}
