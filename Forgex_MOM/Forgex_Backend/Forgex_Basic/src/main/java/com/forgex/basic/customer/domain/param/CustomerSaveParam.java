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
    private Long id;
    private String customerCode;
    private Boolean autoGenerateCode;
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
    private String relatedTenantCode;
    private String transportMode;
    private String paymentTerms;
    private String country;
    private String enterpriseNature;
    private Integer status;
    private String remark;
    private List<CustomerContactDTO> contactList;
    private CustomerInvoiceDTO invoice;
    private CustomerExtraDTO extra;
}
