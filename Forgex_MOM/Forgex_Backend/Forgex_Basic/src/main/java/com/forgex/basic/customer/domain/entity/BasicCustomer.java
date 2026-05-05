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

    /**
     * 客户编码。
     */
    @TableField("customer_code")
    private String customerCode;

    /**
     * 客户简称。
     */
    @TableField("customer_short_name")
    private String customerShortName;

    /**
     * 客户全称。
     */
    @TableField("customer_full_name")
    private String customerFullName;

    /**
     * 客户名称。
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 客户价值等级。
     */
    @TableField("customer_value_level")
    private String customerValueLevel;

    /**
     * 客户信用等级。
     */
    @TableField("customer_credit_level")
    private String customerCreditLevel;

    /**
     * 实际经营地址。
     */
    @TableField("actual_business_address")
    private String actualBusinessAddress;

    /**
     * 经营状态。
     */
    @TableField("business_status")
    private String businessStatus;

    /**
     * 收款地址。
     */
    @TableField("collection_address")
    private String collectionAddress;

    /**
     * 发货地址。
     */
    @TableField("shipping_address")
    private String shippingAddress;

    /**
     * 审批状态。
     */
    @TableField("approval_status")
    private Integer approvalStatus;

    /**
     * 是否关联租户。
     */
    @TableField("is_related_tenant")
    private Boolean isRelatedTenant;

    /**
     * 关联租户编码。
     */
    @TableField("related_tenant_code")
    private String relatedTenantCode;

    /**
     * 运输方式。
     */
    @TableField("transport_mode")
    private String transportMode;

    /**
     * 付款条款。
     */
    @TableField("payment_terms")
    private String paymentTerms;

    /**
     * 国家。
     */
    @TableField("country")
    private String country;

    /**
     * 企业性质。
     */
    @TableField("enterprise_nature")
    private String enterpriseNature;

    /**
     * 状态。
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;

    /**
     * 获取联系人person。
     *
     * @return 字符串结果
     */
    public String getContactPerson() {
        return null;
    }

    /**
     * 获取联系人手机号。
     *
     * @return 字符串结果
     */
    public String getContactPhone() {
        return null;
    }
}
