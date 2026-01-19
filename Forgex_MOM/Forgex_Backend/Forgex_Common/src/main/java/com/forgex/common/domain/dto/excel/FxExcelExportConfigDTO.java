package com.forgex.common.domain.dto.excel;

import lombok.Data;

import java.util.List;

/**
 * Excel导出配置DTO
 * <p>
 * 封装Excel导出配置的完整信息，包括主配置和子项配置。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code id} - 配置ID</li>
 *   <li>{@code tableName} - 表名称</li>
 *   <li>{@code tableCode} - 表编码，用于唯一标识</li>
 *   <li>{@code headerStyleJson} - 表头样式JSON</li>
 *   <li>{@code title} - 标题，支持国际化</li>
 *   <li>{@code subtitle} - 副标题，支持国际化</li>
 *   <li>{@code exportFormat} - 导出格式，如"xlsx"、"xls"等</li>
 *   <li>{@code enableTotal} - 是否启用合计行</li>
 *   <li>{@code version} - 版本号，用于配置版本控制</li>
 *   <li>{@code items} - 导出配置子项列表</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxExcelExportConfigItemDTO
 */
@Data
public class FxExcelExportConfigDTO {
    /**
     * 配置ID
     * <p>导出配置的唯一标识。</p>
     */
    private Long id;

    /**
     * 表名称
     * <p>数据表的名称，用于标识导出的数据源表。</p>
     */
    private String tableName;

    /**
     * 表编码
     * <p>表的唯一标识，用于查询和配置管理。</p>
     */
    private String tableCode;

    /**
     * 表头样式JSON
     * <p>JSON格式的表头样式配置，用于自定义表头外观。</p>
     */
    private String headerStyleJson;

    /**
     * 标题
     * <p>支持国际化的导出配置标题。</p>
     */
    private String title;

    /**
     * 副标题
     * <p>支持国际化的导出配置副标题。</p>
     */
    private String subtitle;

    /**
     * 导出格式
     * <p>指定Excel文件的格式，如"xlsx"（Excel 2007+）、
     * "xls"（Excel 97-2003）等。</p>
     */
    private String exportFormat;

    /**
     * 是否启用合计行
     * <p>为true时，在Excel底部添加合计行。</p>
     */
    private Boolean enableTotal;

    /**
     * 版本号
     * <p>用于配置版本控制，支持配置的增量更新。</p>
     */
    private Integer version;

    /**
     * 导出配置子项列表
     * <p>包含所有导出字段的配置信息。</p>
     */
    private List<FxExcelExportConfigItemDTO> items;
}

