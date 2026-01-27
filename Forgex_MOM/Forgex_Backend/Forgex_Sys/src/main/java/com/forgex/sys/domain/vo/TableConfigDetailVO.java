package com.forgex.sys.domain.vo;

import com.forgex.common.domain.entity.table.FxTableColumnConfig;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 表格配置详情视图对象
 * 包含表格配置和列配置的完整信息
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class TableConfigDetailVO {
    /** 主键ID */
    private Long id;
    
    /** 表编码 */
    private String tableCode;
    
    /** 表名称国际化JSON */
    private String tableNameI18nJson;
    
    /** 表格类型 */
    private String tableType;
    
    /** 行键 */
    private String rowKey;
    
    /** 默认分页大小 */
    private Integer defaultPageSize;
    
    /** 默认排序配置 */
    private String defaultSortJson;
    
    /** 是否启用 */
    private Boolean enabled;
    
    /** 配置版本号 */
    private Integer version;
    
    /** 创建人 */
    private String createBy;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新人 */
    private String updateBy;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 列配置列表 */
    private List<FxTableColumnConfig> columns;
}



