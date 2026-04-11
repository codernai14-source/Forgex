package com.forgex.basic.domain.vo;

import com.forgex.basic.domain.dto.MaterialExtendDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 物料扩展配置视图对象
 * <p>
 * 用于前端表单渲染，在 DTO 基础上增加了类型翻译和解析后的选项列表
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物料扩展配置视图对象")
public class MaterialExtendVO extends MaterialExtendDTO {

    /**
     * 模块名称（如：采购、库存）
     * 对应 module 字段的中文翻译
     */
    @Schema(description = "模块名称")
    private String moduleLabel;

    /**
     * 字段类型名称（如：字符串、数字）
     * 对应 fieldType 字段的中文翻译
     */
    @Schema(description = "字段类型名称")
    private String fieldTypeLabel;

    /**
     * 解析后的选项列表
     * <p>
     * 前端渲染下拉框或多选框时直接使用，结构如：
     * [{ "label": "选项 1", "value": "1" }]
     * </p>
     */
    @Schema(description = "解析后的选项列表")
    private List<Map<String, String>> parsedOptions;
}
