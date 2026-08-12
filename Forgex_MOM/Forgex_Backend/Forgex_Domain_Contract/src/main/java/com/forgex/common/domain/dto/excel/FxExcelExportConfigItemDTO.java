package com.forgex.common.domain.dto.excel;

import lombok.Data;

/**
 * Excel导出配置子项DTO
 * <p>
 * 封装Excel导出配置中单个字段的配置信息。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code id} - 子项ID</li>
 *   <li>{@code configId} - 主配置ID</li>
 *   <li>{@code exportField} - 导出字段名，对应数据表字段</li>
 *   <li>{@code fieldName} - 字段名称，用于显示</li>
 *   <li>{@code i18nJson} - 字段名称国际化JSON</li>
 *   <li>{@code headerStyleJson} - 表头样式JSON</li>
 *   <li>{@code cellStyleJson} - 单元格样式JSON</li>
 *   <li>{@code orderNum} - 排序号，用于指定列顺序</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class FxExcelExportConfigItemDTO {
    /**
     * 子项ID
     * <p>导出配置子项的唯一标识。</p>
     */
    private Long id;

    /**
     * 主配置ID
     * <p>所属的主配置ID，用于关联主配置。</p>
     */
    private Long configId;

    /**
     * 导出字段名
     * <p>对应数据表的实际字段名，用于数据查询和绑定。</p>
     */
    private String exportField;

    /**
     * 字段名称
     * <p>用于显示的字段名称，支持国际化。</p>
     */
    private String fieldName;

    /**
     * 字段名称国际化JSON
     * <p>包含多语言的字段名称配置，用于国际化显示表头。</p>
     */
    private String i18nJson;

    /**
     * 表头样式JSON
     * <p>JSON格式的表头样式配置，用于自定义表头外观。</p>
     */
    private String headerStyleJson;

    /**
     * 单元格样式JSON
     * <p>JSON格式的单元格样式配置，用于自定义单元格外观。</p>
     */
    private String cellStyleJson;

    /**
     * 排序号
     * <p>用于指定列的显示顺序，数值越小越靠前。</p>
     */
    private Integer orderNum;
}

