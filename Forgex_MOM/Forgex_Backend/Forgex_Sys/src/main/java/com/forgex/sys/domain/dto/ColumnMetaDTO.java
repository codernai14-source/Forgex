package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 列元数据DTO
 * <p>
 * 封装数据库表列的详细信息。
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class ColumnMetaDTO {
    /**
     * 列名（数据库字段名）
     */
    private String columnName;
    
    /**
     * 数据类型（如 varchar、int、bigint 等）
     */
    private String dataType;
    
    /**
     * 列类型（完整类型，如 varchar(50)、int(11) 等）
     */
    private String columnType;
    
    /**
     * 列注释
     */
    private String columnComment;
    
    /**
     * 是否主键
     */
    private Boolean isPrimaryKey;
    
    /**
     * 是否自增
     */
    private Boolean isAutoIncrement;
    
    /**
     * 是否可为空
     */
    private Boolean isNullable;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 字符最大长度
     */
    private Long characterMaximumLength;
    
    /**
     * 数值精度
     */
    private Integer numericPrecision;
    
    /**
     * 数值小数位数
     */
    private Integer numericScale;
    
    /**
     * Java 字段名（驼峰命名）
     */
    private String javaFieldName;
    
    /**
     * Java 类型（如 String、Integer、Long 等）
     */
    private String javaType;
    
    /**
     * Java 类型完整路径（如 java.lang.String）
     */
    private String javaTypeFullName;
    
    /**
     * 是否需要导入（基本类型和 java.lang 包下的类不需要导入）
     */
    private Boolean needImport;
    
    /**
     * 是否为基础字段（BaseEntity 中的字段）
     */
    private Boolean isBaseField;
    
    /**
     * 列名（英文）
     */
    private String columnNameEn;
    
    /**
     * 查询类型（input、select、date 等）
     */
    private String queryType;
    
    /**
     * 查询操作符（like、eq、gt、lt 等）
     */
    private String queryOperator;
    
    /**
     * 字典编码
     */
    private String dictCode;
    
    /**
     * 表单类型（input、textarea、number、select、date、datetime 等）
     */
    private String formType;
    
    /**
     * 是否必填
     */
    private Boolean required;
    
    /**
     * 列宽度
     */
    private Integer width;
    
    /**
     * 对齐方式（left、center、right）
     */
    private String align;
    
    /**
     * 是否可排序
     */
    private Boolean sortable;
    
    /**
     * 渲染类型（text、image、link、tag 等）
     */
    private String renderType;
    
    /**
     * 获取首字母大写的 Java 字段名
     */
    public String getJavaFieldNameCapital() {
        if (javaFieldName == null || javaFieldName.isEmpty()) {
            return javaFieldName;
        }
        return Character.toUpperCase(javaFieldName.charAt(0)) + javaFieldName.substring(1);
    }
}

