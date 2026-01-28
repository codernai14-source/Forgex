package com.forgex.common.domain.dto.excel;

import lombok.Data;

import java.util.List;

/**
 * Excel导入配置DTO
 * <p>
 * 封装Excel导入配置的完整信息，包括主配置和子项配置。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code id} - 配置ID</li>
 *   <li>{@code tableName} - 表名称</li>
 *   <li>{@code tableCode} - 表编码，用于唯一标识</li>
 *   <li>{@code title} - 标题，支持国际化</li>
 *   <li>{@code subtitle} - 副标题，支持国际化</li>
 *   <li>{@code version} - 版本号，用于配置版本控制</li>
 *   <li>{@code items} - 导入配置子项列表</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxExcelImportConfigItemDTO
 */
@Data
public class FxExcelImportConfigDTO {
    /**
     * 配置ID
     * <p>导入配置的唯一标识。</p>
     */
    private Long id;

    /**
     * 表名称
     * <p>数据表的名称，用于标识导入的目标表。</p>
     */
    private String tableName;

    /**
     * 表编码
     * <p>表的唯一标识，用于查询和配置管理。</p>
     */
    private String tableCode;

    /**
     * 标题
     * <p>支持国际化的导入配置标题。</p>
     */
    private String title;

    /**
     * 副标题
     * <p>支持国际化的导入配置副标题。</p>
     */
    private String subtitle;

    /**
     * 版本号
     * <p>用于配置版本控制，支持配置的增量更新。</p>
     */
    private Integer version;

    /**
     * 导入配置子项列表
     * <p>包含所有导入字段的配置信息。</p>
     */
    private List<FxExcelImportConfigItemDTO> items;
}

