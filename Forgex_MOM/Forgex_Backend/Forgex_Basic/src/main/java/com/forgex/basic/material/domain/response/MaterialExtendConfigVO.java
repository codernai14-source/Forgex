package com.forgex.basic.material.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 物料扩展配置视图对象
 * <p>
 * 用于前端表单渲染，包含配置信息及解析后的选项数据
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料扩展配置视图对象")
public class MaterialExtendConfigVO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 模块编码
     */
    @Schema(description = "模块编码")
    private String module;

    /**
     * 模块名称（如：采购、库存）
     */
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 字段名称（英文标识）
     */
    @Schema(description = "字段名称")
    private String fieldName;

    /**
     * 字段标签（中文显示）
     */
    @Schema(description = "字段标签")
    private String fieldLabel;

    /**
     * 字段类型（STRING, NUMBER, DATE 等）
     */
    @Schema(description = "字段类型")
    private String fieldType;

    /**
     * 字段类型名称（如：字符串、数字）
     */
    @Schema(description = "字段类型名称")
    private String fieldTypeName;

    /**
     * 解析后的选项列表
     * <p>
     * 前端渲染下拉框时直接使用：[{ "label": "选项 1", "value": "1" }]
     * </p>
     */
    @Schema(description = "解析后的选项列表")
    private List<Map<String, String>> options;

    /**
     * 是否必填（0=否，1=是）
     */
    @Schema(description = "是否必填")
    private Integer required;

    /**
     * 校验规则
     */
    @Schema(description = "校验规则")
    private String validationRule;

    /**
     * 默认值
     */
    @Schema(description = "默认值")
    private String defaultValue;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer orderNum;

    /**
     * 状态（0=禁用，1=启用）
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

