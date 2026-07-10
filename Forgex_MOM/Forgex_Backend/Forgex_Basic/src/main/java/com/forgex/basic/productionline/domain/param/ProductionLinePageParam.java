package com.forgex.basic.productionline.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产线分页查询参数。
 * <p>
 * 支持按车间、产线类型、产线编码/名称、状态及创建时间段组合查询。
 * 时间字段继承自 {@link BaseGetParam}，分别使用 startDate / endDate 字段名（对应前端 startTime / endTime）。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseGetParam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "产线分页查询参数")
public class ProductionLinePageParam extends BaseGetParam {

    /**
     * 所属车间 ID。
     */
    @Schema(description = "所属车间 ID")
    private Long workshopId;

    /**
     * 产线类型（字典：prod_line_type）。
     */
    @Schema(description = "产线类型")
    private String productionLineType;

    /**
     * 产线编码（模糊匹配）。
     */
    @Schema(description = "产线编码")
    private String productionLineCode;

    /**
     * 产线名称（模糊匹配）。
     */
    @Schema(description = "产线名称")
    private String productionLineName;

    /**
     * 状态：0=禁用，1=启用。
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 创建时间起始（字符串，由前端传入并由服务层按需解析）。
     */
    @Schema(description = "创建时间起始")
    private String startTime;

    /**
     * 创建时间截止（字符串，由前端传入并由服务层按需解析）。
     */
    @Schema(description = "创建时间截止")
    private String endTime;
}
