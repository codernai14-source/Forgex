package com.forgex.basic.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物料扩展字段配置传输对象
 * <p>
 * 用于向前端返回物料动态扩展字段的定义信息
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料扩展字段配置")
public class MaterialExtendDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 模块编码（PURCHASE=采购，INVENTORY=库存，PRODUCTION=生产，SALES=销售）
     */
    @Schema(description = "模块编码")
    private String module;

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
     * 字段类型（STRING=字符串，NUMBER=数字，DATE=日期，BOOLEAN=布尔，SELECT=下拉框，MULTI_SELECT=多选）
     */
    @Schema(description = "字段类型")
    private String fieldType;

    /**
     * 字段选项（JSON 格式）
     */
    @Schema(description = "字段选项")
    private String fieldOptions;

    /**
     * 是否必填（0=否，1=是）
     */
    @Schema(description = "是否必填")
    private Integer required;

    /**
     * 校验规则（正则表达式或自定义规则）
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
     * 扩展信息 JSON 数据
     * <p>
     * 用于存储物料在该模块下的实际扩展字段值
     * 格式示例：{"color": "红色", "weight": "2.5", "warranty": "12个月"}
     * </p>
     */
    @Schema(description = "扩展信息 JSON 数据")
    private String extendJson;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 状态（0=禁用，1=启用）
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

