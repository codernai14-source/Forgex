package com.forgex.basic.supplier.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商主数据主表实体。
 * <p>
 * 对应表 {@code basic_supplier}，保存平台共享供应商主数据的核心识别与状态字段。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_supplier")
public class BasicSupplier extends BaseEntity {

    /**
     * 供应商编码，全局唯一。
     */
    @TableField("supplier_code")
    private String supplierCode;

    /**
     * 供应商全称。
     */
    @TableField("supplier_full_name")
    private String supplierFullName;

    /**
     * 供应商简称。
     */
    @TableField("supplier_short_name")
    private String supplierShortName;

    /**
     * 供应商 Logo 图片访问地址。
     */
    @TableField("logo_url")
    private String logoUrl;

    /**
     * 英文名。
     */
    @TableField("english_name")
    private String englishName;

    /**
     * 现地址。
     */
    @TableField("current_address")
    private String currentAddress;

    /**
     * 主联系人。
     */
    @TableField("primary_contact")
    private String primaryContact;

    /**
     * 联系电话。
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 合作状态：1-潜在 2-正式 3-暂停 4-淘汰。
     */
    @TableField("cooperation_status")
    private String cooperationStatus;

    /**
     * 信用等级：A/B/C/D。
     */
    @TableField("credit_level")
    private String creditLevel;

    /**
     * 风险等级。
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 供应商分级：1-战略 2-核心 3-一般。
     */
    @TableField("supplier_level")
    private String supplierLevel;

    /**
     * 关联供应商租户编码。
     */
    @TableField("related_tenant_code")
    private String relatedTenantCode;

    /**
     * 审查状态：0-无需审查 1-未审查 2-审查中 3-已审查。
     */
    @TableField("review_status")
    private Integer reviewStatus;

    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;
}
