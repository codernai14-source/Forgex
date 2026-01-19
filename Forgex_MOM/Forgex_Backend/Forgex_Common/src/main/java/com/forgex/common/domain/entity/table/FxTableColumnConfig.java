package com.forgex.common.domain.entity.table;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 表格列配置实体
 * <p>
 * 封装表格列的完整配置信息，用于动态渲染表格列。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code tableCode} - 表编码，标识列所属的表格</li>
 *   <li>{@code field} - 字段名，对应数据表字段</li>
 *   <li>{@code titleI18nJson} - 列标题国际化JSON</li>
 *   <li>{@code align} - 对齐方式，如"left"、"center"、"right"</li>
 *   <li>{@code width} - 列宽度，单位为像素</li>
 *   <li>{@code fixed} - 是否固定列</li>
 *   <li>{@code ellipsis} - 是否省略过长内容</li>
 *   <li>{@code sortable} - 是否可排序</li>
 *   <li>{@code sorterField} - 排序字段</li>
 *   <li>{@code queryable} - 是否可查询</li>
 *   <li>{@code queryType} - 查询类型</li>
 *   <li>{@code queryOperator} - 查询操作符</li>
 *   <li>{@code dictCode} - 字典编码</li>
 *   <li>{@code renderType} - 渲染类型</li>
 *   <li>{@code permKey} - 权限键</li>
 *   <li>{@code orderNum} - 排序号</li>
 *   <li>{@code enabled} - 是否启用</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableConfig
 */
@Data
@TableName("fx_table_column_config")
public class FxTableColumnConfig extends BaseEntity {
    /**
     * 表编码
     * <p>标识列所属的表格。</p>
     */
    private String tableCode;

    /**
     * 字段名
     * <p>对应数据表的实际字段名，用于数据绑定和排序。</p>
     */
    private String field;

    /**
     * 列标题国际化JSON
     * <p>包含多语言的列标题配置，用于国际化显示。</p>
     */
    private String titleI18nJson;

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
     * 排序号
     * <p>用于指定列的显示顺序，数值越小越靠前。</p>
     */
    private Integer orderNum;

    /**
     * 是否启用
     * <p>标识列配置是否启用，false时不显示该列。</p>
     */
    private Boolean enabled;
}

