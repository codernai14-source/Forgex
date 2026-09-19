package com.forgex.basic.supplier.domain.param;

import com.forgex.common.api.dto.SupplierContactDTO;
import com.forgex.common.api.dto.SupplierDetailDTO;
import com.forgex.common.api.dto.SupplierQualificationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 供应商聚合保存参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Data
@Schema(description = "供应商聚合保存参数")
public class SupplierSaveParam {

    /**
     * 供应商主表 ID，新增时为空。
     */
    @Schema(description = "供应商主表 ID")
    private Long id;

    /**
     * 供应商编码。
     */
    @Schema(description = "供应商编码")
    private String supplierCode;

    /**
     * 新增时是否自动生成供应商编码。
     */
    @Schema(description = "新增时是否自动生成供应商编码")
    private Boolean autoGenerateCode;

    /**
     * 供应商全称。
     */
    @NotBlank(message = "供应商全称不能为空")
    @Schema(description = "供应商全称")
    private String supplierFullName;

    /**
     * 供应商简称。
     */
    @Schema(description = "供应商简称")
    private String supplierShortName;

    /**
     * 供应商 Logo 图片访问地址。
     */
    @Schema(description = "供应商 Logo 图片访问地址")
    private String logoUrl;

    /**
     * 英文名。
     */
    @Schema(description = "英文名")
    private String englishName;

    /**
     * 现地址。
     */
    @Schema(description = "现地址")
    private String currentAddress;

    /**
     * 主联系人。
     */
    @Schema(description = "主联系人")
    private String primaryContact;

    /**
     * 联系电话。
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 合作状态。
     */
    @Schema(description = "合作状态")
    private String cooperationStatus;

    /**
     * 信用等级。
     */
    @Schema(description = "信用等级")
    private String creditLevel;

    /**
     * 风险等级。
     */
    @Schema(description = "风险等级")
    private String riskLevel;

    /**
     * 供应商分级。
     */
    @Schema(description = "供应商分级")
    private String supplierLevel;

    /**
     * 关联租户编号。
     */
    @Schema(description = "关联租户编号")
    private String relatedTenantCode;

    /**
     * 审查状态。
     */
    @Schema(description = "审查状态")
    private Integer reviewStatus;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 详情信息。
     */
    @Schema(description = "详情信息")
    private SupplierDetailDTO detail;

    /**
     * 联系人列表。
     */
    @Schema(description = "联系人列表")
    private List<SupplierContactDTO> contactList;

    /**
     * 资质列表。
     */
    @Schema(description = "资质列表")
    private List<SupplierQualificationDTO> qualificationList;
}
