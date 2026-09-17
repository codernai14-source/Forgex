package com.forgex.common.security.compliance;

/**
 * 信息安全合规等级。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum ComplianceLevel {

    /** 不启用合规门控，保持存量行为 */
    NONE,

    /** 二级基线：口令生命周期与会话收紧 */
    L2,

    /** 三级基线：在 L2 之上叠加 MFA、三员分立与强制访问控制 */
    L3;

    /**
     * 解析配置值，无法识别时回退为 {@link #NONE}。
     *
     * @param value 配置字符串
     * @return 合规等级
     */
    public static ComplianceLevel parse(String value) {
        if (value == null) {
            return NONE;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return NONE;
        }
    }
}
