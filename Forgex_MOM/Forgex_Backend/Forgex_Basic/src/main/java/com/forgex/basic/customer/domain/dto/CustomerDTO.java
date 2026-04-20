package com.forgex.basic.customer.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户数据传输对象
 * <p>
 * 用于向前端返回客户详细信息
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-20
 */
@Data
@Schema(description = "客户信息")
public class CustomerDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 客户编码
     */
    @Schema(description = "客户编码")
    private String customerCode;

    /**
     * 客户名称
     */
    @Schema(description = "客户名称")
    private String customerName;

    /**
     * 客户简称
     */
    @Schema(description = "客户简称")
    private String customerShortName;

    /**
     * 客户类型
     * DOMESTIC=国内，OVERSEAS=海外
     */
    @Schema(description = "客户类型")
    private String customerType;

    /**
     * 国家/地区
     */
    @Schema(description = "国家/地区")
    private String country;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String address;

    /**
     * 联系人
     */
    @Schema(description = "联系人")
    private String contactPerson;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")
    private String contactEmail;

    /**
     * 税号
     */
    @Schema(description = "税号")
    private String taxNumber;

    /**
     * 银行账号
     */
    @Schema(description = "银行账号")
    private String bankAccount;

    /**
     * 付款条件
     */
    @Schema(description = "付款条件")
    private String paymentTerms;

    /**
     * 交货条件
     */
    @Schema(description = "交货条件")
    private String deliveryTerms;

    /**
     * 币种
     */
    @Schema(description = "币种")
    private String currency;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 租户 ID
     */
    @Schema(description = "租户 ID")
    private Long tenantId;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
