package com.forgex.basic.material.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 物料附属信息可视化视图对象。
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-12
 */
@Data
@Schema(description = "物料附属信息可视化视图对象")
public class MaterialExtendViewVO {

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
     * 原始扩展信息 ID。
     */
    @Schema(description = "原始扩展信息 ID")
    private Long extendId;

    /**
     * 原始 JSON。
     */
    @Schema(description = "原始 JSON")
    private String extendJson;

    /**
     * 可视化字段。
     */
    @Schema(description = "可视化字段")
    private List<FieldValue> fields;

    /**
     * 未配置字段，保留旧 JSON 中无法匹配 schema 的 key。
     */
    @Schema(description = "未配置字段")
    private Map<String, Object> unknownValues;

    /**
     * 字段值。
     */
    @Data
    @Schema(description = "字段值")
    public static class FieldValue {

        /**
         * 字段配置 ID。
         */
        @Schema(description = "字段配置 ID")
        private Long configId;

        /**
         * 字段名。
         */
        @Schema(description = "字段名")
        private String fieldName;

        /**
         * 字段标签。
         */
        @Schema(description = "字段标签")
        private String fieldLabel;

        /**
         * 字段类型。
         */
        @Schema(description = "字段类型")
        private String fieldType;

        /**
         * 字段类型名称。
         */
        @Schema(description = "字段类型名称")
        private String fieldTypeName;

        /**
         * 字段选项。
         */
        @Schema(description = "字段选项")
        private List<Map<String, String>> options;

        /**
         * 是否必填。
         */
        @Schema(description = "是否必填")
        private Integer required;

        /**
         * 默认值。
         */
        @Schema(description = "默认值")
        private String defaultValue;

        /**
         * 排序号。
         */
        @Schema(description = "排序号")
        private Integer orderNum;

        /**
         * 当前值。
         */
        @Schema(description = "当前值")
        private Object value;

        /**
         * 当前值显示文本。
         */
        @Schema(description = "当前值显示文本")
        private String displayValue;
    }
}
