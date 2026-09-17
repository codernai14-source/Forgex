package com.forgex.sys.domain.config;

import lombok.Data;

/**
 * 帮助中心上传限制配置。
 * <p>
 * 对应 {@code sys_config} 键 {@code system.help.upload}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 */
@Data
public class HelpUploadConfig {

    /** 操作视频最大体积（MB）。 */
    private long videoMaxSizeMb = 200;

    /**
     * 返回默认配置。
     *
     * @return 默认上传限制
     */
    public static HelpUploadConfig defaults() {
        return new HelpUploadConfig();
    }
}
