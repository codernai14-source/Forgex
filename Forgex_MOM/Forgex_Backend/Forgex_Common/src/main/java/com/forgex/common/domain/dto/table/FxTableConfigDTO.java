package com.forgex.common.domain.dto.table;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 表格配置DTO
 * <p>
 * 封装表格配置的完整信息，用于前端动态渲染表格。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code tableCode} - 表编码，唯一标识一个表格</li>
 *   <li>{@code tableName} - 表名称，支持国际化</li>
 *   <li>{@code tableType} - 表格类型，如"tree"、"table"等</li>
 *   <li>{@code rowKey} - 行键，用于标识表格行的唯一字段</li>
 *   <li>{@code defaultPageSize} - 默认分页大小</li>
 *   <li>{@code defaultSortJson} - 默认排序配置（JSON格式）</li>
 *   <li>{@code columns} - 表格列配置列表</li>
 *   <li>{@code queryFields} - 查询字段配置列表</li>
 *   <li>{@code version} - 配置版本号，用于配置更新控制</li>
 *   <li>{@code enabled} - 是否启用</li>
 *   <li>{@code createBy/createTime} - 创建人和创建时间</li>
 *   <li>{@code updateBy/updateTime} - 更新人和更新时间</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableColumnDTO
 * @see FxTableQueryFieldDTO
 */
@Data
public class FxTableConfigDTO {
    /**
     * 表编码
     * <p>唯一标识一个表格，用于查询和配置管理。</p>
     */
    private String tableCode;

    /**
     * 表名称
     * <p>支持国际化的表名称，用于显示表格标题。</p>
     */
    private String tableName;

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
     * 表格列配置列表
     * <p>包含表格所有列的配置信息，用于渲染表格列。</p>
     */
    private List<FxTableColumnDTO> columns;

    /**
     * 查询字段配置列表
     * <p>包含表格所有可查询字段的配置信息，用于渲染查询条件。</p>
     */
    private List<FxTableQueryFieldDTO> queryFields;

    /**
     * 配置版本号
     * <p>用于配置版本控制，支持配置的增量更新。</p>
     */
    private Integer version;

    /**
     * 是否启用
     * <p>标识表格配置是否启用，false时不显示表格。</p>
     */
    private Boolean enabled;

    /**
     * 创建人
     * <p>创建表格配置的用户标识。</p>
     */
    private String createBy;

    /**
     * 创建时间
     * <p>表格配置的创建时间。</p>
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     * <p>最后更新表格配置的用户标识。</p>
     */
    private String updateBy;

    /**
     * 更新时间
     * <p>表格配置的最后更新时间。</p>
     */
    private LocalDateTime updateTime;
}

