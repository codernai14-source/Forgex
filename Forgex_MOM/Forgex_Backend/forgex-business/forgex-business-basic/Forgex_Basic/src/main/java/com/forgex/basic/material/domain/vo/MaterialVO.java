package com.forgex.basic.material.domain.vo;

import com.forgex.basic.material.domain.dto.MaterialDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料视图对象
 * <p>
 * 用于前端列表展示，在 DTO 基础上增加了字典翻译字段
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物料视图对象")
public class MaterialVO extends MaterialDTO {

    /**
     * 状态名称（如：启用、禁用）
     * 对应 status 字段的中文描述
     */
    @Schema(description = "状态名称")
    private String statusName;

    /**
     * 物料类型名称（如：原材料、成品）
     * 对应 materialType 字段的中文描述
     */
    @Schema(description = "物料类型名称")
    private String typeName;

    /**
     * 物料分类名称
     * 对应 materialCategory 字典编码的中文描述
     */
    @Schema(description = "物料分类名称")
    private String categoryName;
}

