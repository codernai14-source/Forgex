package com.forgex.basic.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 物料更新请求参数类
 * <p>
 * 用于接收前端修改物料时提交的表单数据
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料更新参数")
public class MaterialUpdateParam {

    /**
     * 主键 ID（必填）
     */
    @NotNull(message = "物料 ID 不能为空")
    @Schema(description = "物料 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 物料编码
     */
    @Schema(description = "物料编码")
    private String materialCode;

    /**
     * 物料名称
     */
    @Schema(description = "物料名称")
    private String materialName;

    /**
     * 物料类型
     * RAW_MATERIAL=原材料，FINISHED_GOODS=成品，TOOL=工具，SEMI_FINISHED=半成品，OTHER=其它
     */
    @Schema(description = "物料类型")
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
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
