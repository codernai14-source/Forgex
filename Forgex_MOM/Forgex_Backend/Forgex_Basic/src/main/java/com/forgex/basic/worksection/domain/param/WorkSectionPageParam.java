package com.forgex.basic.worksection.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工段分页查询参数。
 * <p>
 * 支持按车间、产线、工段编码/名称、状态及创建时间段组合查询。
 * 时间字段兼容 {@link BaseGetParam} 的 startDate / endDate，并额外提供 startTime / endTime 字段。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseGetParam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工段分页查询参数")
public class WorkSectionPageParam extends BaseGetParam {

    /**
     * 所属车间 ID。
     */
    @Schema(description = "所属车间 ID")
    private Long workshopId;

    /**
     * 所属产线 ID。
     */
    @Schema(description = "所属产线 ID")
    private Long productionLineId;

    /**
     * 工段编码（模糊匹配）。
     */
    @Schema(description = "工段编码")
    private String workSectionCode;

    /**
     * 工段名称（模糊匹配）。
     */
    @Schema(description = "工段名称")
    private String workSectionName;

    /**
     * 状态：0=禁用，1=启用。
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 创建时间起始（字符串）。
     */
    @Schema(description = "创建时间起始")
    private String startTime;

    /**
     * 创建时间截止（字符串）。
     */
    @Schema(description = "创建时间截止")
    private String endTime;
}
