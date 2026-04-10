package com.forgex.basic.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 物料创建请求参数类
 * <p>
 * 用于接收前端创建物料时提交的表单数据
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料创建参数")
public class MaterialCreateParam {

    /**
     * 物料编码（必填，全局唯一）
     */
    @NotBlank(message = "物料编码不能为空")
    @Schema(description = "物料编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialCode;

    /**
     * 物料名称（必填）
     */
    @NotBlank(message = "物料名称不能为空")
    @Schema(description = "物料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialName;

    /**
     * 物料类型（必填）
     * RAW_MATERIAL=原材料，FINISHED_GOODS=成品，TOOL=工具，SEMI_FINISHED=半成品，OTHER=其它
     */
    @NotBlank(message = "物料类型不能为空")
    @Schema(description = "物料类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialType;

    /**
     * 物料分类（字典编码）
     */
    @Schema(description = "物料分类")
    private String materialCategory;

    /**
     * 规格型号
     */
    @Schema(description = "规格型号")
    private String specification;

    /**
     * 计量单位
     */
    @Schema(description = "计量单位")
    private String unit;

    /**
     * 品牌
     */
    @Schema(description = "品牌")
    private String brand;

    /**
     * 物料图片 URL
     */
    @Schema(description = "物料图片 URL")
    private String imageUrl;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 详细描述
     */
    @Schema(description = "详细描述")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用，1-启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}

