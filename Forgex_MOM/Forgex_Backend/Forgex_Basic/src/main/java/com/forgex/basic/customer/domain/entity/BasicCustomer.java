package com.forgex.basic.customer.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户信息表实体类
 * <p>
 * 对应数据库表：basic_customer
 * 用于存储客户基础信息，包含客户编码、名称、类型、联系方式、财务信息等核心字段
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_customer")
public class BasicCustomer extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客户编码（租户内唯一）
     */
    @TableField("customer_code")
    private String customerCode;

    /**
     * 客户名称
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 客户简称
     */
    @TableField("customer_short_name")
    private String customerShortName;

    /**
     * 客户类型
     * DOMESTIC=国内，OVERSEAS=海外
     */
    @TableField("customer_type")
    private String customerType;

    /**
     * 国家/地区
     */
    @TableField("country")
    private String country;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 详细地址
     */
    @TableField("address")
    private String address;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;

    /**
     * 税号
     */
    @TableField("tax_number")
    private String taxNumber;

    /**
     * 银行账号
     */
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 付款条件
     */
    @TableField("payment_terms")
    private String paymentTerms;

    /**
     * 交货条件
     */
    @TableField("delivery_terms")
    private String deliveryTerms;

    /**
     * 币种（默认 CNY）
     */
    @TableField("currency")
    private String currency;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
