package com.forgex.auth.domain.param;

import lombok.Data;

/**
 * 选择租户请求参数
 */
@Data
public class TenantChoiceParam {
    private Long tenantId; // 选择的租户ID
    private String account; // 当前登录账号
    private String username; // 当前登录用户名（完成最终登录）
}
