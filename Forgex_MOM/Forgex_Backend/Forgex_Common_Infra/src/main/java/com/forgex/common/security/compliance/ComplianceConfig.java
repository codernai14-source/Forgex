package com.forgex.common.security.compliance;

import lombok.Data;

/**
 * 合规等级 JSON 配置。
 * <p>
 * 对应配置键 {@code security.compliance}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see ComplianceContext
 */
@Data
public class ComplianceConfig {

    /**
     * 合规等级：none / L2 / L3。
     */
    private String level = "none";
}
