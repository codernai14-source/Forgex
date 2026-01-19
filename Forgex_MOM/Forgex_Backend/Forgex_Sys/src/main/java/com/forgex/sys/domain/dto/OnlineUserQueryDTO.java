package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 在线用户查询条件 DTO。
 * <p>
 * 用于在线用户列表分页查询的入参承载对象。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
public class OnlineUserQueryDTO {

    /**
     * 当前页（从 1 开始）。
     */
    private Long current;

    /**
     * 每页大小。
     */
    private Long size;

    /**
     * 账号（模糊匹配）。
     */
    private String account;

    /**
     * 租户ID（可选；优先使用 {@link com.forgex.common.tenant.TenantContext} 中的租户）。
     */
    private Long tenantId;
}
