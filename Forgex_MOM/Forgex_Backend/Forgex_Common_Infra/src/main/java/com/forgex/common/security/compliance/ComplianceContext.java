package com.forgex.common.security.compliance;

import com.forgex.common.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 合规等级只读门面。
 * <p>
 * 启动时从配置库读取 {@code security.compliance}，供 MFA、三员分立、强制访问控制等门控判断。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see ComplianceLevel
 */
@Component
public class ComplianceContext {

    private static final String CONFIG_KEY = "security.compliance";

    private static volatile ComplianceLevel level = ComplianceLevel.NONE;

    @Resource
    private ConfigService configService;

    @Value("${forgex.security.compliance.level:none}")
    private String environmentLevel;

    /**
     * 初始化合规等级。
     */
    @PostConstruct
    public void initialize() {
        ComplianceConfig config = configService.getGlobalJson(CONFIG_KEY, ComplianceConfig.class, new ComplianceConfig());
        String configured = config == null ? null : config.getLevel();
        if (configured == null || configured.isBlank()) {
            configured = configService.getString(CONFIG_KEY + ".level", environmentLevel);
        }
        level = ComplianceLevel.parse(configured);
    }

    /**
     * 返回当前合规等级。
     *
     * @return 合规等级
     */
    public static ComplianceLevel level() {
        return level;
    }

    /**
     * 判断当前等级是否不低于指定等级。
     *
     * @param required 要求等级
     * @return true 表示已达到
     */
    public static boolean atLeast(ComplianceLevel required) {
        return level.ordinal() >= required.ordinal();
    }
}
