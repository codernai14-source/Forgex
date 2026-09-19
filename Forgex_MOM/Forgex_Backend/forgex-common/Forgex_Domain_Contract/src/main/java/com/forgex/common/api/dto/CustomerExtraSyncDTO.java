package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 客户扩展同步 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Data
public class CustomerExtraSyncDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long customerId;

    private String officialWebsite;

    private String switchboardPhone;

    private String officialEmailDomain;

    private String faxNumber;

    private String socialMediaAccount;

    private Integer equityPenetrationLevel;

    private String holdingRelationFlag;

    private String relatedEnterpriseIds;

    private String groupCustomerLevel;

    private String channelPartnerLevel;

    private LocalDate cooperationAuthStartDate;

    private LocalDate cooperationAuthEndDate;

    private String nationalIndustryCode;

    private String customIndustryCategory;

    private BigDecimal registeredCapital;

    private String registeredCapitalCurrency;

    private BigDecimal paidInCapital;

    private String paidInCapitalCurrency;

    private LocalDate businessTermStart;

    private LocalDate businessTermEnd;

    private String registrationAuthority;

    private String businessScope;
}
