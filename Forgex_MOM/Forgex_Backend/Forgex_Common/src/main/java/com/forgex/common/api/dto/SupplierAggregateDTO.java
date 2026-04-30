package com.forgex.common.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商聚合 DTO
 * <p>
 * 统一封装供应商主表、详情、联系人和资质信息，作为跨模块主数据对象。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@Data
public class SupplierAggregateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 供应商主表 ID
     */
    private Long id;

    /**
     * 公共租户 ID
     */
    private Long tenantId;

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 供应商全称
     */
    private String supplierFullName;

    /**
     * 供应商简称
     */
    private String supplierShortName;

    /**
     * 供应商 Logo 图片访问地址。
     */
    private String logoUrl;

    /**
     * 英文名
     */
    private String englishName;

    /**
     * 现地址
     */
    private String currentAddress;

    /**
     * 主联系人
     */
    private String primaryContact;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 合作状态
     */
    private String cooperationStatus;

    /**
     * 信用等级
     */
    private String creditLevel;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 分级
     */
    private String supplierLevel;

    /**
     * 关联租户编号
     */
    private String relatedTenantCode;

    /**
     * 是否已关联租户。
     */
    private Boolean hasRelatedTenant;

    /**
     * 审查状态
     */
    private Integer reviewStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 详情信息
     */
    private SupplierDetailDTO detail;

    /**
     * 联系人列表
     */
    private List<SupplierContactDTO> contactList;

    /**
     * 资质列表
     */
    private List<SupplierQualificationDTO> qualificationList;
}
