package com.forgex.basic.supplier.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_supplier")
public class BasicSupplier extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("supplier_code")
    private String supplierCode;

    @TableField("supplier_name")
    private String supplierName;

    @TableField("supplier_short_name")
    private String supplierShortName;

    @TableField("supplier_type")
    private String supplierType;

    @TableField("country")
    private String country;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("address")
    private String address;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contact_email")
    private String contactEmail;

    @TableField("tax_number")
    private String taxNumber;

    @TableField("bank_account")
    private String bankAccount;

    @TableField("payment_terms")
    private String paymentTerms;

    @TableField("quality_level")
    private String qualityLevel;

    @TableField("certification")
    private String certification;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;
}
