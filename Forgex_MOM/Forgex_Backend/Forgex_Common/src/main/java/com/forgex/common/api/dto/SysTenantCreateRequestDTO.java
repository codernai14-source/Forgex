package com.forgex.common.api.dto;

import com.forgex.common.enums.TenantTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 租户创建请求 DTO
 * <p>
 * 作为基础数据等模块调用系统模块内部租户创建接口的最小公共契约。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SysTenantCreateRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户名称
     */
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    /**
     * 租户编码
     */
    @NotBlank(message = "租户编码不能为空")
    private String tenantCode;

    /**
     * 描述
     */
    private String description;

    /**
     * Logo
     */
    private String logo;

    /**
     * 租户类型
     */
    @NotNull(message = "租户类型不能为空")
    private TenantTypeEnum tenantType;

    /**
     * 状态
     */
    private Boolean status;
}
