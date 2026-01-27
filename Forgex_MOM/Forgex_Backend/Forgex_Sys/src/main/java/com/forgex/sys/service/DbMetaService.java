package com.forgex.sys.service;

import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;

import java.util.List;

/**
 * 数据库元数据服务接口
 * <p>
 * 提供数据库表结构查询功能，用于代码生成器读取表信息。
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
public interface DbMetaService {
    
    /**
     * 查询数据库中的所有表
     * 
     * @return 表名列表
     */
    List<String> listTables();
    
    /**
     * 查询表的元数据信息
     * 
     * @param tableName 表名
     * @return 表元数据
     */
    TableMetaDTO getTableMeta(String tableName);
    
    /**
     * 查询表的所有列信息
     * 
     * @param tableName 表名
     * @return 列元数据列表
     */
    List<ColumnMetaDTO> listColumns(String tableName);
}

