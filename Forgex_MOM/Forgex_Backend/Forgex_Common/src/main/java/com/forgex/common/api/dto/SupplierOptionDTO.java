package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 供应商下拉选项 DTO
 * <p>
 * 用于其它服务或页面获取轻量级供应商选择项。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierOptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 供应商主表 ID
     */
    private Long id;

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 供应商全称
     */
    private String supplierFullName;

    /**
     * 供应商简称
     */
    private String supplierShortName;

    /**
     * 关联租户编号
     */
    private String relatedTenantCode;

    /**
     * 是否已经关联租户。
     */
    private Boolean hasRelatedTenant;

    /**
     * 审查状态
     */
    private Integer reviewStatus;
}
