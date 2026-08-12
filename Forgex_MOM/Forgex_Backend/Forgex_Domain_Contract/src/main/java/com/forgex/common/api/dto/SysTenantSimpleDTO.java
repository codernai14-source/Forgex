package com.forgex.common.api.dto;

import com.forgex.common.enums.TenantTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户简要信息 DTO
 * <p>
 * 供跨模块内部调用返回租户核心字段，避免公共模块依赖系统模块 DTO。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SysTenantSimpleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID
     */
    private Long id;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 租户编码
     */
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
    private TenantTypeEnum tenantType;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
