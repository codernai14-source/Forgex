package com.forgex.basic.customer.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户主数据聚合 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@Schema(description = "客户主数据")
public class CustomerDTO {
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
    private Boolean autoGenerateCode;
    private List<CustomerContactDTO> contactList;
    private CustomerInvoiceDTO invoice;
    private CustomerExtraDTO extra;
    private String createBy;
    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
