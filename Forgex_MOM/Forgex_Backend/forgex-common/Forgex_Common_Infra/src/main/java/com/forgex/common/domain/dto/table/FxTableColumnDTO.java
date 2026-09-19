package com.forgex.common.domain.dto.table;

import lombok.Data;

/**
 * 表格列配置DTO
 * <p>
 * 封装表格列的配置信息，用于动态渲染表格列。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code field} - 字段名，对应数据表字段</li>
 *   <li>{@code title} - 列标题，支持国际化</li>
 *   <li>{@code align} - 对齐方式，如"left"、"center"、"right"</li>
 *   <li>{@code width} - 列宽度，单位为像素</li>
 *   <li>{@code fixed} - 是否固定列，true时不参与列宽自适应</li>
 *   <li>{@code ellipsis} - 是否省略过长内容，true时显示省略号</li>
 *   <li>{@code sortable} - 是否可排序</li>
 *   <li>{@code sorterField} - 排序字段，用于指定实际排序的字段名</li>
 *   <li>{@code queryable} - 是否可查询，true时显示在查询条件中</li>
 *   <li>{@code queryType} - 查询类型，如"input"、"select"等</li>
 *   <li>{@code queryOperator} - 查询操作符，如"like"、"eq"等</li>
 *   <li>{@code dictCode} - 字典编码，用于渲染下拉选择</li>
 *   <li>{@code renderType} - 渲染类型，如"text"、"image"、"link"等</li>
 *   <li>{@code permKey} - 权限键，用于控制列的显示权限</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableConfigDTO
 */
@Data
public class FxTableColumnDTO {
    /**
     * 字段名
     * <p>对应数据表的实际字段名，用于数据绑定和排序。</p>
     */
    private String field;

    /**
     * 列标题
     * <p>支持国际化的列标题，用于显示表头。</p>
     */
    private String title;

    /**
     * 对齐方式
     * <p>指定列内容的对齐方式，如"left"（左对齐）、
     * "center"（居中）、"right"（右对齐）。</p>
     */
    private String align;

    /**
     * 列宽度
     * <p>指定列的宽度，单位为像素（px）。</p>
     */
    private Integer width;

    /**
     * 是否固定列
     * <p>为true时，该列不参与列宽自适应，保持固定宽度。</p>
     */
    private String fixed;

    /**
     * 是否省略过长内容
     * <p>为true时，当内容超过列宽时显示省略号（...）。</p>
     */
    private Boolean ellipsis;

    /**
     * 是否可排序
     * <p>为true时，该列支持点击表头进行排序。</p>
     */
    private Boolean sortable;

    /**
     * 排序字段
     * <p>指定实际用于排序的字段名，可能与field不同。</p>
     */
    private String sorterField;

    /**
     * 是否可查询
     * <p>为true时，该列会显示在查询条件表单中。</p>
     */
    private Boolean queryable;

    /**
     * 查询类型
     * <p>当queryable为true时，指定查询输入框的类型，
     * 如"input"（文本输入）、"select"（下拉选择）、"date"（日期选择）等。</p>
     */
    private String queryType;

    /**
     * 查询操作符
     * <p>当queryable为true时，指定查询条件的操作符，
     * 如"like"（模糊查询）、"eq"（等于）、"gt"（大于）、"lt"（小于）等。</p>
     */
    private String queryOperator;

    /**
     * 字典编码
     * <p>当查询类型为"select"时，指定字典编码，
     * 用于渲染下拉选择框的选项。</p>
     */
    private String dictCode;

    /**
     * 字典字段
     * <p>指定字典翻译后的字段名，用于显示字典标签。
     * 例如：如果field为"status"，dictField为"statusText"，
     * 则会自动解析statusText字段的JSON字符串并显示标签。</p>
     */
    private String dictField;

    /**
     * 渲染类型
     * <p>指定列内容的渲染方式，如"text"（普通文本）、
     * "image"（图片）、"link"（链接）、"tag"（标签）等。</p>
     */
    private String renderType;

    /**
     * 权限键
     * <p>用于控制该列的显示权限，只有拥有对应权限的用户才能看到该列。</p>
     */
    private String permKey;

    /**
     * 是否显示
     * <p>用于用户个性化配置，控制该列是否显示在表格中。</p>
     * <p>true时显示该列，false时隐藏该列。</p>
     */
    private Boolean visible;

    /**
     * 排序顺序
     * <p>用于用户个性化配置，控制该列在表格中的显示顺序。</p>
     * <p>数值越小排在越前面。</p>
     */
    private Integer order;
}

