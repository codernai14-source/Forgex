package com.forgex.basic.material.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物料附属字段状态参数。
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-12
 */
@Data
@Schema(description = "物料附属字段状态参数")
public class MaterialExtendFieldStatusParam {

    /**
     * 字段配置 ID。
     */
    @Schema(description = "字段配置 ID")
    private Long id;

    /**
     * 状态（0=禁用，1=启用）。
     */
    @Schema(description = "状态")
    private Integer status;
}
