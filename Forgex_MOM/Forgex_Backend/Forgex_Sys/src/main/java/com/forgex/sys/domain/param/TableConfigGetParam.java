package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 表格配置查询参数
 * 
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class TableConfigGetParam {
    /** 主键ID */
    private Long id;
    
    /** 表格代码 */
    private String tableCode;
    
    /** 当前页码 */
    private Long current;
    
    /** 每页大小 */
    private Long size;
    
    /** 表格名称 */
    private String tableName;
    
    /** 表格类型 */
    private String tableType;
    
    /** 是否启用 */
    private Boolean enabled;
}

