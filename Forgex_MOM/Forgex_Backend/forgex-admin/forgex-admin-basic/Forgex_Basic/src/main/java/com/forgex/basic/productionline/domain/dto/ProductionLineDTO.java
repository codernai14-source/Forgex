package com.forgex.basic.productionline.domain.dto;

import com.forgex.basic.productionline.domain.entity.BasicProductionLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产线主数据传输对象。
 * <p>
 * 继承 {@link BasicProductionLine} 实体，并扩展车间与负责人相关的展示字段。
 * 当前版本已通过实体自身快照字段（workshopCode / workshopName / managerEmployeeNo / managerEmployeeName）
 * 覆盖车间和负责人的展示需求，DTO 暂不新增业务字段，后续如需扩展可在此处追加。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BasicProductionLine
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "产线数据传输对象")
public class ProductionLineDTO extends BasicProductionLine {

    /**
     * 扩展字段：所属工厂 ID（来自车间）。在服务层额外填充，便于前端展示工厂层级。
     */
    @Schema(description = "所属工厂 ID")
    private Long factoryId;

    /**
     * 扩展字段：所属工厂编码（来自车间）。在服务层额外填充，便于前端展示工厂层级。
     */
    @Schema(description = "所属工厂编码")
    private String factoryCode;

    /**
     * 扩展字段：所属工厂名称（来自车间）。在服务层额外填充，便于前端展示工厂层级。
     */
    @Schema(description = "所属工厂名称")
    private String factoryName;
}
