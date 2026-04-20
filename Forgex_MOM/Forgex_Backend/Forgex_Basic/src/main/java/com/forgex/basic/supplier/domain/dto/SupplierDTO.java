package com.forgex.basic.supplier.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商数据传输对象
 * <p>
 * 用于向前端返回供应商详细信息
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-20
 */
@Data
@Schema(description = "供应商信息")
public class SupplierDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 供应商编码
     */
    @Schema(description = "供应商编码")
    private String supplierCode;

    /**
     * 供应商名称
     */
    @Schema(description = "供应商名称")
    private String supplierName;

    /**
     * 供应商简称
     */
    @Schema(description = "供应商简称")
    private String supplierShortName;

    /**
     * 供应商类型
     */
    @Schema(description = "供应商类型")
    private String supplierType;

    /**
     * 国家
     */
    @Schema(description = "国家")
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
     * 地址
     */
    @Schema(description = "地址")
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
     * 付款条款
     */
    @Schema(description = "付款条款")
    private String paymentTerms;

    /**
     * 质量等级
     */
    @Schema(description = "质量等级")
    private String qualityLevel;

    /**
     * 认证信息
     */
    @Schema(description = "认证信息")
    private String certification;

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
