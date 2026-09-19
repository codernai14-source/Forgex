package com.forgex.basic.customer.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 客户扩展数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class CustomerExtraDTO {
    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 客户 ID。
     */
    private Long customerId;
    /**
     * 官方网站。
     */
    private String officialWebsite;
    /**
     * 总机电话。
     */
    private String switchboardPhone;
    /**
     * 官方邮箱域名。
     */
    private String officialEmailDomain;
    /**
     * 传真号。
     */
    private String faxNumber;
    /**
     * 社媒账号。
     */
    private String socialMediaAccount;
    /**
     * 股权穿透等级。
     */
    private Integer equityPenetrationLevel;
    /**
     * 控股关系标识。
     */
    private String holdingRelationFlag;
    /**
     * 关联企业 ID 集合。
     */
    private String relatedEnterpriseIds;
    /**
     * 集团客户等级。
     */
    private String groupCustomerLevel;
    /**
     * 渠道伙伴等级。
     */
    private String channelPartnerLevel;
    /**
     * 合作认证开始日期。
     */
    private LocalDate cooperationAuthStartDate;
    /**
     * 合作认证结束日期。
     */
    private LocalDate cooperationAuthEndDate;
    /**
     * 国标行业编码。
     */
    private String nationalIndustryCode;
    /**
     * 自定义行业分类。
     */
    private String customIndustryCategory;
    /**
     * 注册资本。
     */
    private BigDecimal registeredCapital;
    /**
     * 注册资本币种。
     */
    private String registeredCapitalCurrency;
    /**
     * 实缴资本。
     */
    private BigDecimal paidInCapital;
    /**
     * 实缴资本币种。
     */
    private String paidInCapitalCurrency;
    /**
     * 营业期限开始日期。
     */
    private LocalDate businessTermStart;
    /**
     * 营业期限结束日期。
     */
    private LocalDate businessTermEnd;
    /**
     * 登记机关。
     */
    private String registrationAuthority;
    /**
     * 经营范围。
     */
    private String businessScope;
}
