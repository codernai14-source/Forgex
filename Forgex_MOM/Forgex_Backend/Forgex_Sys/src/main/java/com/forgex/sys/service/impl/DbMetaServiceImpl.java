package com.forgex.sys.service.impl;

import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;
import com.forgex.sys.service.DbMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * 数据库元数据服务实现类
 * <p>
 * 通过 JDBC 查询 information_schema 获取表结构信息。
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DbMetaServiceImpl implements DbMetaService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<String> listTables() {
        String sql = "SELECT TABLE_NAME FROM information_schema.TABLES " +
                     "WHERE TABLE_SCHEMA = (SELECT DATABASE()) " +
                     "ORDER BY TABLE_NAME";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public TableMetaDTO getTableMeta(String tableName) {
        String sql = "SELECT TABLE_NAME, TABLE_COMMENT, ENGINE, TABLE_COLLATION, CREATE_TIME " +
                     "FROM information_schema.TABLES " +
                     "WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = ?";
        
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            TableMetaDTO meta = new TableMetaDTO();
            meta.setTableName(rs.getString("TABLE_NAME"));
            meta.setTableComment(rs.getString("TABLE_COMMENT"));
            meta.setEngine(rs.getString("ENGINE"));
            meta.setCharset(rs.getString("TABLE_COLLATION"));
            meta.setCreateTime(rs.getString("CREATE_TIME"));
            return meta;
        }, tableName);
    }

    @Override
    public List<ColumnMetaDTO> listColumns(String tableName) {
        String sql = "SELECT " +
                     "  COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, COLUMN_COMMENT, " +
                     "  COLUMN_KEY, EXTRA, IS_NULLABLE, COLUMN_DEFAULT, " +
                     "  CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE " +
                     "FROM information_schema.COLUMNS " +
                     "WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = ? " +
                     "ORDER BY ORDINAL_POSITION";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ColumnMetaDTO column = new ColumnMetaDTO();
            
            // 数据库字段信息
            String columnName = rs.getString("COLUMN_NAME");
            column.setColumnName(columnName);
            column.setDataType(rs.getString("DATA_TYPE"));
            column.setColumnType(rs.getString("COLUMN_TYPE"));
            column.setColumnComment(rs.getString("COLUMN_COMMENT"));
            column.setIsPrimaryKey("PRI".equals(rs.getString("COLUMN_KEY")));
            column.setIsAutoIncrement("auto_increment".equals(rs.getString("EXTRA")));
            column.setIsNullable("YES".equals(rs.getString("IS_NULLABLE")));
            column.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
            column.setCharacterMaximumLength(rs.getLong("CHARACTER_MAXIMUM_LENGTH"));
            column.setNumericPrecision((Integer) rs.getObject("NUMERIC_PRECISION"));
            column.setNumericScale((Integer) rs.getObject("NUMERIC_SCALE"));
            
            // 转换为 Java 字段名和类型
            column.setJavaFieldName(toJavaFieldName(columnName));
            String javaType = toJavaType(rs.getString("DATA_TYPE"));
            column.setJavaType(javaType);
            column.setJavaTypeFullName(toJavaTypeFullName(rs.getString("DATA_TYPE")));
            column.setNeedImport(needImport(javaType));
            
            return column;
        }, tableName);
    }

    /**
     * 将数据库字段名转换为 Java 字段名（驼峰命名）
     * 
     * @param columnName 数据库字段名
     * @return Java 字段名
     */
    private String toJavaFieldName(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            return columnName;
        }
        
        String[] parts = columnName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    sb.append(parts[i].substring(1));
                }
            }
        }
        
        return sb.toString();
    }

    /**
     * 将数据库类型转换为 Java 类型（简单类名）
     * 
     * @param dbType 数据库类型
     * @return Java 类型
     */
    private String toJavaType(String dbType) {
        if (dbType == null) {
            return "String";
        }
        
        String type = dbType.toLowerCase(Locale.ROOT);
        
        switch (type) {
            case "tinyint":
            case "smallint":
            case "mediumint":
            case "int":
            case "integer":
                return "Integer";
            case "bigint":
                return "Long";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "decimal":
            case "numeric":
                return "BigDecimal";
            case "bit":
            case "boolean":
                return "Boolean";
            case "date":
                return "LocalDate";
            case "time":
                return "LocalTime";
            case "datetime":
            case "timestamp":
                return "LocalDateTime";
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "mediumtext":
            case "longtext":
            default:
                return "String";
        }
    }

    /**
     * 将数据库类型转换为 Java 类型（完整类名）
     * 
     * @param dbType 数据库类型
     * @return Java 类型完整路径
     */
    private String toJavaTypeFullName(String dbType) {
        String simpleType = toJavaType(dbType);
        
        switch (simpleType) {
            case "BigDecimal":
                return "java.math.BigDecimal";
            case "LocalDate":
                return "java.time.LocalDate";
            case "LocalTime":
                return "java.time.LocalTime";
            case "LocalDateTime":
                return "java.time.LocalDateTime";
            default:
                return "java.lang." + simpleType;
        }
    }

    /**
     * 判断 Java 类型是否需要导入
     * 
     * @param javaType Java 类型
     * @return 是否需要导入
     */
    private Boolean needImport(String javaType) {
        // 基本类型和 java.lang 包下的类不需要导入
        return "BigDecimal".equals(javaType) || 
               "LocalDate".equals(javaType) || 
               "LocalTime".equals(javaType) || 
               "LocalDateTime".equals(javaType);
    }
}



