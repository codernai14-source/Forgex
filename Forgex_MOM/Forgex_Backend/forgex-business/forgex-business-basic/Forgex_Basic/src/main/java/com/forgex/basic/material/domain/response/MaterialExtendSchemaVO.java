package com.forgex.basic.material.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 物料附属字段结构视图对象。
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-12
 */
@Data
@Schema(description = "物料附属字段结构视图对象")
public class MaterialExtendSchemaVO {

    /**
     * 结构 ID。
     */
    @Schema(description = "结构 ID")
    private Long id;

    /**
     * 模块编码。
     */
    @Schema(description = "模块编码")
    private String module;

    /**
     * 模块名称。
     */
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 物料类型。
     */
    @Schema(description = "物料类型")
    private String materialType;

    /**
     * 结构 JSON。
     */
    @Schema(description = "结构 JSON")
    private String schemaJson;

    /**
     * 版本号。
     */
    @Schema(description = "版本号")
    private Integer version;

    /**
     * 状态。
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 字段列表。
     */
    @Schema(description = "字段列表")
    private List<MaterialExtendConfigVO> fields;
}
