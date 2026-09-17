package com.forgex.sys.domain.dto.tenant;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 租户初始化结果。
 * <p>仅在创建事务内部短暂携带管理员初始凭据，用于向父租户发送一次性通知。</p>
 */
@Data
@AllArgsConstructor
public class TenantInitResult {
    private Long administratorUserId;
    private String administratorAccount;
    private String initialPassword;
}
