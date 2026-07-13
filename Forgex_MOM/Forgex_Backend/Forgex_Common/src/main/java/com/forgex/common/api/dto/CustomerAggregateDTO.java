package com.forgex.common.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户聚合 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Data
public class CustomerAggregateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private String customerCode;

    private String customerShortName;

    private String customerFullName;

    private String customerName;

    private String customerValueLevel;

    private String customerCreditLevel;

    private String actualBusinessAddress;

    private String businessStatus;

    private String collectionAddress;

    private String shippingAddress;

    private Integer approvalStatus;

    private Boolean isRelatedTenant;

    private Boolean hasRelatedTenant;

    private String relatedTenantCode;

    private String transportMode;

    private String paymentTerms;

    private String country;

    private String enterpriseNature;

    private Integer status;

    private String remark;

    private String createBy;

    @com.forgex.common.api.annotation.AutoFillUsername(userIdField = "createBy")
    private String createByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private List<CustomerContactSyncDTO> contactList;

    private CustomerInvoiceSyncDTO invoice;

    private CustomerExtraSyncDTO extra;
}
