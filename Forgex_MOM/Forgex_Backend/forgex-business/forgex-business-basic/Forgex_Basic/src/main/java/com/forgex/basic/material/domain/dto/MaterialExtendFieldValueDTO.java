package com.forgex.basic.material.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 物料附属信息结构化值。
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-12
 */
@Data
@Schema(description = "物料附属信息结构化值")
public class MaterialExtendFieldValueDTO {

    /**
     * 所属模块。
     */
    @Schema(description = "所属模块")
    private String module;

    /**
     * 字段值集合，key 为字段名。
     */
    @Schema(description = "字段值集合")
    private Map<String, Object> values;
}
