package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 表元数据DTO
 * <p>
 * 封装数据库表的基本信息。
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class TableMetaDTO {
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表注释
     */
    private String tableComment;
    
    /**
     * 表引擎
     */
    private String engine;
    
    /**
     * 字符集
     */
    private String charset;
    
    /**
     * 创建时间
     */
    private String createTime;
}



