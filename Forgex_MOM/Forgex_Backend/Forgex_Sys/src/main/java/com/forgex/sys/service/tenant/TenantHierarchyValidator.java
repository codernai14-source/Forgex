package com.forgex.sys.service.tenant;

import com.forgex.common.enums.TenantTypeEnum;

/**
 * 租户父子层级校验器。
 * <p>系统只允许主租户作为其它租户的父租户。</p>
 */
public final class TenantHierarchyValidator {

    private TenantHierarchyValidator() {
    }

    /**
     * 校验租户类型与父租户信息是否匹配。
     *
     * @param tenantType 租户类型
     * @param parentTenantId 父租户 ID
     * @param parentIsMainTenant 父租户是否为主租户
     */
    public static void validate(TenantTypeEnum tenantType, Long parentTenantId, boolean parentIsMainTenant) {
        if (tenantType == null) {
            throw new IllegalArgumentException("租户类别不能为空");
        }
        if (TenantTypeEnum.MAIN_TENANT.equals(tenantType)) {
            if (parentTenantId != null) {
                throw new IllegalArgumentException("主租户不能设置父租户");
            }
            return;
        }
        if (parentTenantId == null) {
            throw new IllegalArgumentException("非主租户必须选择父租户");
        }
        if (!parentIsMainTenant) {
            throw new IllegalArgumentException("父租户只能选择主租户");
        }
    }
}
