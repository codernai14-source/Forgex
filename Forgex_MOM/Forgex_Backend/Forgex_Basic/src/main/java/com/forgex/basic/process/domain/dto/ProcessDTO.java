package com.forgex.basic.process.domain.dto;

import com.forgex.basic.process.domain.entity.BasicProcess;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序主数据传输对象。
 * <p>
 * 继承 {@link BasicProcess} 实体，并扩展工厂维度信息。
 * 工段、产线、车间相关的展示信息已通过实体自身快照字段覆盖，DTO 仅在服务层额外补充工厂维度字段。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BasicProcess
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工序数据传输对象")
public class ProcessDTO extends BasicProcess {

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
