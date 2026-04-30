package com.forgex.basic.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户主数据主表实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_customer")
public class BasicCustomer extends BaseEntity {

    @TableField("customer_code")
    private String customerCode;

    @TableField("customer_short_name")
    private String customerShortName;

    @TableField("customer_full_name")
    private String customerFullName;

    @TableField("customer_name")
    private String customerName;

    @TableField("customer_value_level")
    private String customerValueLevel;

    @TableField("customer_credit_level")
    private String customerCreditLevel;

    @TableField("actual_business_address")
    private String actualBusinessAddress;

    @TableField("business_status")
    private String businessStatus;

    @TableField("collection_address")
    private String collectionAddress;

    @TableField("shipping_address")
    private String shippingAddress;

    @TableField("approval_status")
    private Integer approvalStatus;

    @TableField("is_related_tenant")
    private Boolean isRelatedTenant;

    @TableField("related_tenant_code")
    private String relatedTenantCode;

    @TableField("transport_mode")
    private String transportMode;

    @TableField("payment_terms")
    private String paymentTerms;

    @TableField("country")
    private String country;

    @TableField("enterprise_nature")
    private String enterpriseNature;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    public String getContactPerson() {
        return null;
    }

    public String getContactPhone() {
        return null;
    }
}
