package com.forgex.basic.material.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物料附属字段结构查询参数。
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-12
 */
@Data
@Schema(description = "物料附属字段结构查询参数")
public class MaterialExtendSchemaQueryParam {

    /**
     * 模块编码。
     */
    @Schema(description = "模块编码")
    private String module;

    /**
     * 物料类型。
     */
    @Schema(description = "物料类型")
    private String materialType;

    /**
     * 页码。
     */
    @Schema(description = "页码")
    private Integer pageNum = 1;

    /**
     * 每页条数。
     */
    @Schema(description = "每页条数")
    private Integer pageSize = 20;
}
