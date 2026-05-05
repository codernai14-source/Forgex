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
    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 租户 ID。
     */
    private Long tenantId;
    /**
     * 客户编码。
     */
    private String customerCode;
    /**
     * 客户简称。
     */
    private String customerShortName;
    /**
     * 客户全称。
     */
    private String customerFullName;
    /**
     * 客户名称。
     */
    private String customerName;
    /**
     * 客户价值等级。
     */
    private String customerValueLevel;
    /**
     * 客户信用等级。
     */
    private String customerCreditLevel;
    /**
     * 实际经营地址。
     */
    private String actualBusinessAddress;
    /**
     * 经营状态。
     */
    private String businessStatus;
    /**
     * 收款地址。
     */
    private String collectionAddress;
    /**
     * 发货地址。
     */
    private String shippingAddress;
    /**
     * 审批状态。
     */
    private Integer approvalStatus;
    /**
     * 是否关联租户。
     */
    private Boolean isRelatedTenant;
    /**
     * 是否已关联租户。
     */
    private Boolean hasRelatedTenant;
    /**
     * 关联租户编码。
     */
    private String relatedTenantCode;
    /**
     * 运输方式。
     */
    private String transportMode;
    /**
     * 付款条款。
     */
    private String paymentTerms;
    /**
     * 国家。
     */
    private String country;
    /**
     * 企业性质。
     */
    private String enterpriseNature;
    /**
     * 状态。
     */
    private Integer status;
    /**
     * 备注。
     */
    private String remark;
    /**
     * 是否自动生成编码。
     */
    private Boolean autoGenerateCode;
    /**
     * 联系人列表。
     */
    private List<CustomerContactDTO> contactList;
    /**
     * 开票信息。
     */
    private CustomerInvoiceDTO invoice;
    /**
     * 扩展信息。
     */
    private CustomerExtraDTO extra;
    /**
     * 创建人。
     */
    private String createBy;
    /**
     * 更新人。
     */
    private String updateBy;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
