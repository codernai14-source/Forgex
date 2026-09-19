package com.forgex.common.license;

/**
 * 授权状态枚举
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum LicenseStatus {

    /**
     * 未授权。
     */
    UNLICENSED,

    /**
     * 已授权且有效。
     */
    VALID,

    /**
     * 即将到期。
     */
    EXPIRING_SOON,

    /**
     * 已进入宽限期。
     */
    GRACE,

    /**
     * 已过期。
     */
    EXPIRED,

    /**
     * 授权无效。
     */
    INVALID
}
