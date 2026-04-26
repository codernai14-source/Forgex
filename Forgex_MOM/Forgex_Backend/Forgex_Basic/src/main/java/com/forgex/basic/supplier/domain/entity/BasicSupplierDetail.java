package com.forgex.basic.supplier.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.forgex.common.crypto.FieldEncrypt;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 供应商详情信息实体。
 * <p>
 * 对应表 {@code basic_supplier_detail}，保存供应商工商、税务、发票等明细信息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_supplier_detail")
public class BasicSupplierDetail extends BaseEntity {

    /**
     * 供应商主表 ID。
     */
    @TableField("supplier_id")
    private Long supplierId;

    /**
     * 法人代表。
     */
    @TableField("legal_representative")
    private String legalRepresentative;

    /**
     * 注册资本。
     */
    @TableField("registered_capital")
    private BigDecimal registeredCapital;

    /**
     * 成立日期。
     */
    @TableField("establishment_date")
    private LocalDate establishmentDate;

    /**
     * 企业性质。
     */
    @TableField("enterprise_nature")
    private String enterpriseNature;

    /**
     * 行业分类。
     */
    @TableField("industry_category")
    private String industryCategory;

    /**
     * 注册地址。
     */
    @TableField("registered_address")
    private String registeredAddress;

    /**
     * 经营地址。
     */
    @TableField("business_address")
    private String businessAddress;

    /**
     * 邮箱。
     */
    @TableField("email")
    private String email;

    /**
     * 税号。
     */
    @TableField("tax_number")
    private String taxNumber;

    /**
     * 开户银行。
     */
    @TableField("bank_name")
    private String bankName;

    /**
     * 银行账号，加密存储。
     */
    @FieldEncrypt
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 发票类型。
     */
    @TableField("invoice_type")
    private String invoiceType;

    /**
     * 默认税率。
     */
    @TableField("default_tax_rate")
    private BigDecimal defaultTaxRate;
}
