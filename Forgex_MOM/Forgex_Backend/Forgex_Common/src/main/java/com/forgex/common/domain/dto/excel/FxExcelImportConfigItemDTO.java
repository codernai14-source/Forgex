package com.forgex.common.domain.dto.excel;

import lombok.Data;

/**
 * Excel 导入配置子项 DTO
 * <p>
 * 封装 Excel 导入配置中单个字段的配置信息。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code id} - 子项 ID</li>
 *   <li>{@code configId} - 主配置 ID</li>
 *   <li>{@code i18nJson} - 字段名称国际化 JSON</li>
 *   <li>{@code importField} - 导入字段名，对应 Excel 列名</li>
 *   <li>{@code fieldType} - 字段类型，如"string"、"number"、"date"等</li>
 *   <li>{@code dictCode} - 字典编码，用于渲染下拉选择</li>
 *   <li>{@code dataSourceType} - 数据来源类型</li>
 *   <li>{@code dataSourceValue} - 数据来源值</li>
 *   <li>{@code dependsOnFieldKey} - 依赖的父字段 key</li>
 *   <li>{@code separator} - 多选分隔符</li>
 *   <li>{@code fieldRemark} - 字段备注/说明</li>
 *   <li>{@code required} - 是否必填</li>
 *   <li>{@code orderNum} - 排序号，用于指定字段顺序</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 * @see FxExcelImportConfigItem
 */
@Data
public class FxExcelImportConfigItemDTO {
    /**
     * 子项 ID
     * <p>导入配置子项的唯一标识。</p>
     */
    private Long id;

    /**
     * 主配置 ID
     * <p>所属的主配置 ID，用于关联主配置。</p>
     */
    private Long configId;

    /**
     * 字段名称国际化 JSON
     * <p>包含多语言的字段名称配置，用于国际化显示。</p>
     */
    private String i18nJson;

    /**
     * 导入字段名
     * <p>对应 Excel 列名，用于从 Excel 中读取数据。</p>
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
     * 数据来源类型
     * <p>
     * 可选值：NONE(无下拉), DICT(字典下拉), JSON(静态下拉), PROVIDER(接口下拉)
     * </p>
     *
     * @since 1.1.0
     * @see FxExcelImportConfigItem#getDataSourceType()
     */
    private String dataSourceType;

    /**
     * 数据来源值
     * <p>
     * - DICT 类型：字典父表 id（如 '189'）
     * - JSON 类型：JSON 数组字符串（如 '["0|停用","1|启用"]'）
     * - PROVIDER 类型：Provider 编码（如 'FACTORY_LIST'）
     * </p>
     *
     * @since 1.1.0
     * @see FxExcelImportConfigItem#getDataSourceValue()
     */
    private String dataSourceValue;

    /**
     * 依赖的父字段 key（级联下拉时使用）
     * <p>
     * 例如：部门字段依赖工厂字段，则填写工厂字段的 importField
     * </p>
     *
     * @since 1.1.0
     * @see FxExcelImportConfigItem#getDependsOnFieldKey()
     */
    private String dependsOnFieldKey;

    /**
     * 多选分隔符
     * <p>
     * 当字段支持多选时，使用此分隔符连接多个值
     * </p>
     *
     * @since 1.1.0
     * @see FxExcelImportConfigItem#getSeparator()
     */
    private String separator;

    /**
     * 字段备注/说明
     * <p>
     * 用于 Excel 表头显示 additional 信息
     * </p>
     *
     * @since 1.1.0
     * @see FxExcelImportConfigItem#getFieldRemark()
     */
    private String fieldRemark;

    /**
     * 是否必填
     * <p>为 true 时，该字段为必填项，不能为空。</p>
     */
    private Boolean required;

    /**
     * 排序号
     * <p>用于指定字段的显示顺序，数值越小越靠前。</p>
     */
    private Integer orderNum;
}
