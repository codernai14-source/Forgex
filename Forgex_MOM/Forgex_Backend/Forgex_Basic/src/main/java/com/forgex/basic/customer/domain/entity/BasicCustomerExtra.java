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
    @TableField("customer_id")
    private Long customerId;
    @TableField("official_website")
    private String officialWebsite;
    @TableField("switchboard_phone")
    private String switchboardPhone;
    @TableField("official_email_domain")
    private String officialEmailDomain;
    @TableField("fax_number")
    private String faxNumber;
    @TableField("social_media_account")
    private String socialMediaAccount;
    @TableField("equity_penetration_level")
    private Integer equityPenetrationLevel;
    @TableField("holding_relation_flag")
    private String holdingRelationFlag;
    @TableField("related_enterprise_ids")
    private String relatedEnterpriseIds;
    @TableField("group_customer_level")
    private String groupCustomerLevel;
    @TableField("channel_partner_level")
    private String channelPartnerLevel;
    @TableField("cooperation_auth_start_date")
    private LocalDate cooperationAuthStartDate;
    @TableField("cooperation_auth_end_date")
    private LocalDate cooperationAuthEndDate;
    @TableField("national_industry_code")
    private String nationalIndustryCode;
    @TableField("custom_industry_category")
    private String customIndustryCategory;
    @TableField("registered_capital")
    private BigDecimal registeredCapital;
    @TableField("registered_capital_currency")
    private String registeredCapitalCurrency;
    @TableField("paid_in_capital")
    private BigDecimal paidInCapital;
    @TableField("paid_in_capital_currency")
    private String paidInCapitalCurrency;
    @TableField("business_term_start")
    private LocalDate businessTermStart;
    @TableField("business_term_end")
    private LocalDate businessTermEnd;
    @TableField("registration_authority")
    private String registrationAuthority;
    @TableField("business_scope")
    private String businessScope;
}
