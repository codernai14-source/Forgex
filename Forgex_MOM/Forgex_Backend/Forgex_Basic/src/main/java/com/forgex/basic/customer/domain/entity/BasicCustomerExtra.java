package com.forgex.basic.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 客户其它信息实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_customer_extra")
public class BasicCustomerExtra extends BaseEntity {
    /**
     * 客户 ID。
     */
    @TableField("customer_id")
    private Long customerId;
    /**
     * 官方网站。
     */
    @TableField("official_website")
    private String officialWebsite;
    /**
     * 总机电话。
     */
    @TableField("switchboard_phone")
    private String switchboardPhone;
    /**
     * 官方邮箱域名。
     */
    @TableField("official_email_domain")
    private String officialEmailDomain;
    /**
     * 传真号。
     */
    @TableField("fax_number")
    private String faxNumber;
    /**
     * 社媒账号。
     */
    @TableField("social_media_account")
    private String socialMediaAccount;
    /**
     * 股权穿透等级。
     */
    @TableField("equity_penetration_level")
    private Integer equityPenetrationLevel;
    /**
     * 控股关系标识。
     */
    @TableField("holding_relation_flag")
    private String holdingRelationFlag;
    /**
     * 关联企业 ID 集合。
     */
    @TableField("related_enterprise_ids")
    private String relatedEnterpriseIds;
    /**
     * 集团客户等级。
     */
    @TableField("group_customer_level")
    private String groupCustomerLevel;
    /**
     * 渠道伙伴等级。
     */
    @TableField("channel_partner_level")
    private String channelPartnerLevel;
    /**
     * 合作认证开始日期。
     */
    @TableField("cooperation_auth_start_date")
    private LocalDate cooperationAuthStartDate;
    /**
     * 合作认证结束日期。
     */
    @TableField("cooperation_auth_end_date")
    private LocalDate cooperationAuthEndDate;
    /**
     * 国标行业编码。
     */
    @TableField("national_industry_code")
    private String nationalIndustryCode;
    /**
     * 自定义行业分类。
     */
    @TableField("custom_industry_category")
    private String customIndustryCategory;
    /**
     * 注册资本。
     */
    @TableField("registered_capital")
    private BigDecimal registeredCapital;
    /**
     * 注册资本币种。
     */
    @TableField("registered_capital_currency")
    private String registeredCapitalCurrency;
    /**
     * 实缴资本。
     */
    @TableField("paid_in_capital")
    private BigDecimal paidInCapital;
    /**
     * 实缴资本币种。
     */
    @TableField("paid_in_capital_currency")
    private String paidInCapitalCurrency;
    /**
     * 营业期限开始日期。
     */
    @TableField("business_term_start")
    private LocalDate businessTermStart;
    /**
     * 营业期限结束日期。
     */
    @TableField("business_term_end")
    private LocalDate businessTermEnd;
    /**
     * 登记机关。
     */
    @TableField("registration_authority")
    private String registrationAuthority;
    /**
     * 经营范围。
     */
    @TableField("business_scope")
    private String businessScope;
}
