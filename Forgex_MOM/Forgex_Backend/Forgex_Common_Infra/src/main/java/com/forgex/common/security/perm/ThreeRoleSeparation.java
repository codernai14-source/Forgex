package com.forgex.common.security.perm;

import java.util.Set;

/**
 * 三员分立保留角色与互斥矩阵。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public final class ThreeRoleSeparation {

    /** 系统管理员 */
    public static final String SYS_ADMIN = "SYS_ADMIN";

    /** 安全管理员 */
    public static final String SEC_ADMIN = "SEC_ADMIN";

    /** 审计管理员 */
    public static final String AUDIT_ADMIN = "AUDIT_ADMIN";

    /** 兼容历史 auditor 角色键 */
    public static final String AUDITOR = "auditor";

    /** 兼容历史 admin 角色键，在 L3 下视为系统管理员 */
    public static final String ADMIN = "admin";

    private static final Set<String> RESERVED = Set.of(SYS_ADMIN, SEC_ADMIN, AUDIT_ADMIN, AUDITOR, ADMIN);

    private ThreeRoleSeparation() {
    }

    /**
     * 判断是否为保留角色。
     *
     * @param roleKey 角色键
     * @return true 表示保留角色
     */
    public static boolean isReserved(String roleKey) {
        return roleKey != null && RESERVED.contains(roleKey);
    }

    /**
     * 归一化角色族：admin/SYS_ADMIN、auditor/AUDIT_ADMIN。
     *
     * @param roleKey 角色键
     * @return 归一化族
     */
    public static String family(String roleKey) {
        if (ADMIN.equals(roleKey) || SYS_ADMIN.equals(roleKey)) {
            return SYS_ADMIN;
        }
        if (AUDITOR.equals(roleKey) || AUDIT_ADMIN.equals(roleKey)) {
            return AUDIT_ADMIN;
        }
        return roleKey;
    }

    /**
     * 审计只读权限前缀。
     *
     * @param permKey 权限码
     * @return true 表示审计权限
     */
    public static boolean isAuditPermission(String permKey) {
        return permKey != null && (permKey.contains("operation-log") || permKey.contains("loginLog") || permKey.contains("login-log"));
    }

    /**
     * 安全配置权限。
     *
     * @param permKey 权限码
     * @return true 表示安全配置权限
     */
    public static boolean isSecurityPermission(String permKey) {
        return permKey != null && (permKey.startsWith("sys:kms") || permKey.startsWith("sys:security") || permKey.startsWith("sys:backup"));
    }
}
