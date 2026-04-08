package com.forgex.common.domain.dto.table;

import lombok.Data;

/**
 * 表格查询字段DTO
 * <p>
 * 封装表格查询字段的配置信息，用于渲染查询条件表单。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code field} - 字段名，对应数据表字段</li>
 *   <li>{@code label} - 字段标签，支持国际化</li>
 *   <li>{@code queryType} - 查询类型，如"input"、"select"、"date"等</li>
 *   <li>{@code queryOperator} - 查询操作符，如"like"、"eq"、"gt"等</li>
 *   <li>{@code dictCode} - 字典编码，用于渲染下拉选择</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableConfigDTO
 */
@Data
public class FxTableQueryFieldDTO {
    /**
     * 字段名
     * <p>对应数据表的实际字段名，用于构建查询条件。</p>
     */
    private String field;

    /**
     * 字段标签
     * <p>支持国际化的字段标签，用于显示查询条件标题。</p>
     */
    private String label;

    /**
     * 查询类型
     * <p>指定查询输入框的类型，如"input"（文本输入）、
     * "select"（下拉选择）、"date"（日期选择）等。</p>
     */
    private String queryType;

    /**
     * 查询操作符
     * <p>指定查询条件的操作符，如"like"（模糊查询）、
     * "eq"（等于）、"gt"（大于）、"lt"（小于）等。</p>
     */
    private String queryOperator;

    /**
     * 字典编码
     * <p>当查询类型为"select"时，指定字典编码，
     * 用于渲染下拉选择框的选项。</p>
     */
    private String dictCode;
}

