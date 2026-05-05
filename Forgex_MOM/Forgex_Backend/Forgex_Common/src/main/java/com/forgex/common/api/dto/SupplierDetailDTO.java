package com.forgex.common.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 供应商详情 DTO
 * <p>
 * 封装供应商详情表的一对一业务字段。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 明细主表 ID
     */
    private Long id;

    /**
     * 供应商主表 ID
     */
    private Long supplierId;

    /**
     * 法人代表
     */
    private String legalRepresentative;

    /**
     * 注册资本。
     */
    private BigDecimal registeredCapital;

    /**
     * 成立日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate establishmentDate;

    /**
     * 企业性质
     */
    private String enterpriseNature;

    /**
     * 行业分类
     */
    private String industryCategory;

    /**
     * 注册地址。
     */
    private String registeredAddress;

    /**
     * 经营地址
     */
    private String businessAddress;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 税号。
     */
    private String taxNumber;

    /**
     * 开户银行。
     */
    private String bankName;

    /**
     * 银行账号。
     */
    private String bankAccount;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 默认税率
     */
    private BigDecimal defaultTaxRate;
}
