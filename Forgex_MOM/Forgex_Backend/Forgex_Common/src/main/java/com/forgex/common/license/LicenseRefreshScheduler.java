package com.forgex.common.license;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 授权刷新调度器
 * <p>
 * 在系统管理与任务调度服务中定期刷新授权缓存，确保宽限期和到期状态及时变化。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "forgex.license", name = "scheduler-enabled", havingValue = "true")
public class LicenseRefreshScheduler {

    private final LicenseManager licenseManager;

    public LicenseRefreshScheduler(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    /**
     * 定时刷新当前授权状态。
     */
    @Scheduled(fixedDelayString = "${forgex.license.refresh-interval-ms:900000}")
    public void refresh() {
        try {
            licenseManager.refresh();
        } catch (Exception ex) {
            log.warn("刷新授权缓存失败: {}", ex.getMessage());
        }
    }
}
