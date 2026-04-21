/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service.impl;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.ColumnMetaDTO;
import com.forgex.sys.domain.dto.TableMetaDTO;
import com.forgex.sys.domain.entity.SysCodegenDatasource;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.DbMetaService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 数据库元数据服务实现
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Service
public class DbMetaServiceImpl implements DbMetaService {

    @Override
    public List<String> listSchemas(SysCodegenDatasource datasource) {
        String sql = "SELECT schema_name FROM information_schema.schemata ORDER BY schema_name";
        return queryList(datasource, sql);
    }

    @Override
    public List<String> listTables(SysCodegenDatasource datasource, String schemaName) {
        String sql = "SELECT TABLE_NAME FROM information_schema.TABLES " +
            "WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME";
        return queryList(datasource, sql, normalizeSchema(datasource, schemaName));
    }

    @Override
    public TableMetaDTO getTableMeta(SysCodegenDatasource datasource, String schemaName, String tableName) {
        String sql = "SELECT TABLE_NAME, TABLE_COMMENT, ENGINE, TABLE_COLLATION, CREATE_TIME " +
            "FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        try (Connection connection = openConnection(datasource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizeSchema(datasource, schemaName));
            statement.setString(2, tableName);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_TABLE_NOT_FOUND, tableName);
                }
                TableMetaDTO meta = new TableMetaDTO();
                meta.setTableName(rs.getString("TABLE_NAME"));
                meta.setTableComment(rs.getString("TABLE_COMMENT"));
                meta.setEngine(rs.getString("ENGINE"));
                meta.setCharset(rs.getString("TABLE_COLLATION"));
                meta.setCreateTime(rs.getString("CREATE_TIME"));
                return meta;
            }
        } catch (I18nBusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_TABLE_NOT_FOUND, tableName);
        }
    }

    @Override
    public List<ColumnMetaDTO> listColumns(SysCodegenDatasource datasource, String schemaName, String tableName) {
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, COLUMN_COMMENT, COLUMN_KEY, EXTRA, " +
            "IS_NULLABLE, COLUMN_DEFAULT, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE " +
            "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
        try (Connection connection = openConnection(datasource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizeSchema(datasource, schemaName));
            statement.setString(2, tableName);
            try (ResultSet rs = statement.executeQuery()) {
                List<ColumnMetaDTO> columns = new ArrayList<>();
                while (rs.next()) {
                    ColumnMetaDTO column = new ColumnMetaDTO();
                    String columnName = rs.getString("COLUMN_NAME");
                    column.setColumnName(columnName);
                    column.setDataType(rs.getString("DATA_TYPE"));
                    column.setColumnType(rs.getString("COLUMN_TYPE"));
                    column.setColumnComment(rs.getString("COLUMN_COMMENT"));
                    column.setIsPrimaryKey("PRI".equals(rs.getString("COLUMN_KEY")));
                    column.setIsAutoIncrement("auto_increment".equalsIgnoreCase(rs.getString("EXTRA")));
                    column.setIsNullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
                    column.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
                    column.setCharacterMaximumLength((Long) rs.getObject("CHARACTER_MAXIMUM_LENGTH"));
                    column.setNumericPrecision((Integer) rs.getObject("NUMERIC_PRECISION"));
                    column.setNumericScale((Integer) rs.getObject("NUMERIC_SCALE"));
                    column.setJavaFieldName(toJavaFieldName(columnName));
                    String javaType = toJavaType(column.getDataType());
                    column.setJavaType(javaType);
                    column.setJavaTypeFullName(toJavaTypeFullName(javaType));
                    column.setNeedImport(needImport(javaType));
                    columns.add(column);
                }
                return columns;
            }
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_TABLE_NOT_FOUND, tableName);
        }
    }

    private List<String> queryList(SysCodegenDatasource datasource, String sql, Object... params) {
        try (Connection connection = openConnection(datasource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<String> values = new ArrayList<>();
                while (rs.next()) {
                    values.add(rs.getString(1));
                }
                return values;
            }
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_DATASOURCE_TEST_FAILED, ex.getMessage());
        }
    }

    private Connection openConnection(SysCodegenDatasource datasource) throws Exception {
        return DriverManager.getConnection(datasource.getJdbcUrl(), datasource.getUsername(), datasource.getPassword());
    }

    private String normalizeSchema(SysCodegenDatasource datasource, String schemaName) {
        if (StringUtils.hasText(schemaName)) {
            return schemaName;
        }
        if (StringUtils.hasText(datasource.getSchemaName())) {
            return datasource.getSchemaName();
        }
        throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CODEGEN_PARAM_EMPTY);
    }

    private String toJavaFieldName(String columnName) {
        if (!StringUtils.hasText(columnName)) {
            return columnName;
        }
        String[] parts = columnName.toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                builder.append(parts[i].substring(1));
            }
        }
        return builder.toString();
    }

    private String toJavaType(String dbType) {
        if (!StringUtils.hasText(dbType)) {
            return "String";
        }
        return switch (dbType.toLowerCase(Locale.ROOT)) {
            case "tinyint", "smallint", "mediumint", "int", "integer" -> "Integer";
            case "bigint" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            case "decimal", "numeric" -> "BigDecimal";
            case "bit", "boolean" -> "Boolean";
            case "date" -> "LocalDate";
            case "datetime", "timestamp" -> "LocalDateTime";
            case "time" -> "LocalTime";
            default -> "String";
        };
    }

    private String toJavaTypeFullName(String javaType) {
        return switch (javaType) {
            case "BigDecimal" -> "java.math.BigDecimal";
            case "LocalDate" -> "java.time.LocalDate";
            case "LocalDateTime" -> "java.time.LocalDateTime";
            case "LocalTime" -> "java.time.LocalTime";
            default -> "java.lang." + javaType;
        };
    }

    private Boolean needImport(String javaType) {
        return !"String".equals(javaType)
            && !"Long".equals(javaType)
            && !"Integer".equals(javaType)
            && !"Float".equals(javaType)
            && !"Double".equals(javaType)
            && !"Boolean".equals(javaType);
    }
}
