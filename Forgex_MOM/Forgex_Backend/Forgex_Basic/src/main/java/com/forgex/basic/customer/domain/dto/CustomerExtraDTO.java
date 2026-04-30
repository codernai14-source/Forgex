package com.forgex.basic.customer.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerExtraDTO {
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
