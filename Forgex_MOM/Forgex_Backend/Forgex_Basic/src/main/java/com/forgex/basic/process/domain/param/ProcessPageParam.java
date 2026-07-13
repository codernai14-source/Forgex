package com.forgex.basic.process.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序分页查询参数。
 * <p>
 * 支持按工段、车间、产线、工序编码/名称、状态及创建时间段组合查询。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseGetParam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工序分页查询参数")
public class ProcessPageParam extends BaseGetParam {

    /**
     * 所属工段 ID。
     */
    @Schema(description = "所属工段 ID")
    private Long workSectionId;

    /**
     * 所属车间 ID（冗余字段，便于按车间过滤）。
     */
    @Schema(description = "所属车间 ID")
    private Long workshopId;

    /**
     * 所属产线 ID（冗余字段，便于按产线过滤）。
     */
    @Schema(description = "所属产线 ID")
    private Long productionLineId;

    /**
     * 工序编码（模糊匹配）。
     */
    @Schema(description = "工序编码")
    private String processCode;

    /**
     * 工序名称（模糊匹配）。
     */
    @Schema(description = "工序名称")
    private String processName;

    /**
     * 工序类型（字典：process_type）。
     */
    @Schema(description = "工序类型")
    private String processType;

    /**
     * 报工方式（字典：report_type）。
     */
    @Schema(description = "报工方式")
    private String reportType;

    /**
     * 质检触发点（字典：qc_trigger_point）。
     */
    @Schema(description = "质检触发点")
    private String qcTriggerPoint;

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
