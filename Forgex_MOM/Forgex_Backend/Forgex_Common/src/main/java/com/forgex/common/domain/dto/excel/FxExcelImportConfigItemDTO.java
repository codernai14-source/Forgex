package com.forgex.common.domain.dto.excel;

import lombok.Data;

/**
 * Excel导入配置子项DTO
 * <p>
 * 封装Excel导入配置中单个字段的配置信息。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code id} - 子项ID</li>
 *   <li>{@code configId} - 主配置ID</li>
 *   <li>{@code i18nJson} - 字段名称国际化JSON</li>
 *   <li>{@code importField} - 导入字段名，对应Excel列名</li>
 *   <li>{@code fieldType} - 字段类型，如"string"、"number"、"date"等</li>
 *   <li>{@code dictCode} - 字典编码，用于渲染下拉选择</li>
 *   <li>{@code required} - 是否必填</li>
 *   <li>{@code orderNum} - 排序号，用于指定字段顺序</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class FxExcelImportConfigItemDTO {
    /**
     * 子项ID
     * <p>导入配置子项的唯一标识。</p>
     */
    private Long id;

    /**
     * 主配置ID
     * <p>所属的主配置ID，用于关联主配置。</p>
     */
    private Long configId;

    /**
     * 字段名称国际化JSON
     * <p>包含多语言的字段名称配置，用于国际化显示。</p>
     */
    private String i18nJson;

    /**
     * 导入字段名
     * <p>对应Excel列名，用于从Excel中读取数据。</p>
     */
    private String importField;

    /**
     * 字段类型
     * <p>指定字段的数据类型，如"string"（字符串）、
     * "number"（数字）、"date"（日期）等。</p>
     */
    private String fieldType;

    /**
     * 字典编码
     * <p>当字段类型为枚举时，指定字典编码，
     * 用于渲染下拉选择框和验证数据有效性。</p>
     */
    private String dictCode;

    /**
     * 是否必填
     * <p>为true时，该字段为必填项，不能为空。</p>
     */
    private Boolean required;

    /**
     * 排序号
     * <p>用于指定字段的显示顺序，数值越小越靠前。</p>
     */
    private Integer orderNum;
}

