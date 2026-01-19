package com.forgex.common.domain.entity.table;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 表格配置实体
 * <p>
 * 封装表格的完整配置信息，用于动态渲染表格。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code tableCode} - 表编码，唯一标识一个表格</li>
 *   <li>{@code tableNameI18nJson} - 表名称国际化JSON</li>
 *   <li>{@code tableType} - 表格类型，如"tree"、"table"等</li>
 *   <li>{@code rowKey} - 行键，用于标识表格行的唯一字段</li>
 *   <li>{@code defaultPageSize} - 默认分页大小</li>
 *   <li>{@code defaultSortJson} - 默认排序配置（JSON格式）</li>
 *   <li>{@code enabled} - 是否启用</li>
 *   <li>{@code version} - 配置版本号</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableColumnConfig
 */
@Data
@TableName("fx_table_config")
public class FxTableConfig extends BaseEntity {
    /**
     * 表编码
     * <p>唯一标识一个表格，用于查询和配置管理。</p>
     */
    private String tableCode;

    /**
     * 表名称国际化JSON
     * <p>包含多语言的表名称配置，用于国际化显示表格标题。</p>
     */
    private String tableNameI18nJson;

    /**
     * 表格类型
     * <p>指定表格的类型，如"tree"（树形表格）、"table"（普通表格）等。</p>
     */
    private String tableType;

    /**
     * 行键
     * <p>用于标识表格行的唯一字段，默认为"id"。</p>
     */
    private String rowKey;

    /**
     * 默认分页大小
     * <p>指定表格的默认每页显示条数，默认为20。</p>
     */
    private Integer defaultPageSize;

    /**
     * 默认排序配置
     * <p>JSON格式的默认排序配置，用于初始化表格的排序状态。</p>
     */
    private String defaultSortJson;

    /**
     * 是否启用
     * <p>标识表格配置是否启用，false时不显示表格。</p>
     */
    private Boolean enabled;

    /**
     * 配置版本号
     * <p>用于配置版本控制，支持配置的增量更新。</p>
     */
    private Integer version;
}

