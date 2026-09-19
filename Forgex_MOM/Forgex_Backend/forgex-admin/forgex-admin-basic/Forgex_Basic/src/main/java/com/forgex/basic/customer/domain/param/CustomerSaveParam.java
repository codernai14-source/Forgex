package com.forgex.basic.customer.domain.param;

import com.forgex.basic.customer.domain.dto.CustomerContactDTO;
import com.forgex.basic.customer.domain.dto.CustomerExtraDTO;
import com.forgex.basic.customer.domain.dto.CustomerInvoiceDTO;
import lombok.Data;

import java.util.List;

/**
 * 客户主数据聚合保存参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
public class CustomerSaveParam {
    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 客户编码。
     */
    private String customerCode;
    /**
     * 是否自动生成编码。
     */
    private Boolean autoGenerateCode;
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
}
