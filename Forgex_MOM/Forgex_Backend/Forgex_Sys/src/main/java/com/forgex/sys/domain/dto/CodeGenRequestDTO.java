package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 代码生成请求DTO
 * <p>封装代码生成所需的参数信息。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class CodeGenRequestDTO {
    /**
     * 数据库表名
     */
    private String tableName;

    /**
     * 生成模式（SINGLE、TREE、MASTER_DETAIL、DETAIL）
     */
    private String mode;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 基础包名
     */
    private String basePackage;

    /**
     * 实体类名称
     */
    private String entityName;

    /**
     * 父ID字段名（树形结构使用）
     */
    private String parentIdField;

    /**
     * 树名称字段名（树形结构使用）
     */
    private String treeNameField;
}

