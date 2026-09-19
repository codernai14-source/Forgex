package com.forgex.basic.worksection.domain.dto;

import com.forgex.basic.worksection.domain.entity.BasicWorkSection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工段主数据传输对象。
 * <p>
 * 继承 {@link BasicWorkSection} 实体，并扩展工厂维度信息。
 * 车间与产线相关的展示信息已通过实体自身快照字段（workshopCode / workshopName / productionLineCode / productionLineName）覆盖，
 * DTO 仅在服务层额外补充工厂维度字段。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BasicWorkSection
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工段数据传输对象")
public class WorkSectionDTO extends BasicWorkSection {

    /**
     * 扩展字段：所属工厂 ID（来自车间）。在服务层额外填充。
     */
    @Schema(description = "所属工厂 ID")
    private Long factoryId;

    /**
     * 扩展字段：所属工厂编码（来自车间）。在服务层额外填充。
     */
    @Schema(description = "所属工厂编码")
    private String factoryCode;

    /**
     * 扩展字段：所属工厂名称（来自车间）。在服务层额外填充。
     */
    @Schema(description = "所属工厂名称")
    private String factoryName;
}
