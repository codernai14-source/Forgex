/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgex.common.crypto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库透明加密（TDE）配置检测工具。
 * <p>
 * 检测当前数据库的 TDE 配置状态，支持 MySQL、PostgreSQL 和达梦数据库。
 * <p>
 * 检测内容：
 * <ul>
 *   <li>密钥环插件是否加载</li>
 *   <li>默认表空间加密是否开启</li>
 *   <li>已加密的表列表</li>
 *   <li>加密相关变量配置</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * TdeConfigChecker.TdeStatus status = TdeConfigChecker.check(dataSource);
 * System.out.println("TDE 已启用: " + status.isEnabled());
 * System.out.println("加密表: " + status.getEncryptedTables());
 * </pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
public final class TdeConfigChecker {

    private TdeConfigChecker() {
        // 工具类不允许实例化
    }

    /**
     * 检测数据库 TDE 配置状态。
     *
     * @param dataSource 数据源
     * @return TDE 状态信息
     */
    public static TdeStatus check(DataSource dataSource) {
        TdeStatus status = new TdeStatus();
        try (Connection conn = dataSource.getConnection()) {
            String dbProduct = conn.getMetaData().getDatabaseProductName().toLowerCase();
            status.setDatabaseType(dbProduct);

            if (dbProduct.contains("mysql") || dbProduct.contains("mariadb")) {
                checkMySQL(conn, status);
            } else if (dbProduct.contains("postgresql")) {
                checkPostgreSQL(conn, status);
            } else if (dbProduct.contains("dm") || dbProduct.contains("达梦")) {
                checkDM(conn, status);
            } else {
                status.setMessage("Unsupported database: " + dbProduct);
            }
        } catch (Exception e) {
            log.error("TDE 配置检测失败", e);
            status.setMessage("TDE check failed: " + e.getMessage());
        }
        return status;
    }

    /**
     * 检测 MySQL TDE 配置。
     */
    private static void checkMySQL(Connection conn, TdeStatus status) throws SQLException {
        // 1. 检查密钥环插件
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT PLUGIN_NAME, PLUGIN_STATUS FROM INFORMATION_SCHEMA.PLUGINS " +
                             "WHERE PLUGIN_NAME LIKE 'keyring%'")) {
            while (rs.next()) {
                String name = rs.getString("PLUGIN_NAME");
                String pluginStatus = rs.getString("PLUGIN_STATUS");
                status.setKeyringPlugin(name + " (" + pluginStatus + ")");
                if ("ACTIVE".equalsIgnoreCase(pluginStatus)) {
                    status.setKeyringLoaded(true);
                }
            }
        }

        // 2. 检查默认表空间加密
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT @@default_table_encryption AS val")) {
            if (rs.next()) {
                String val = rs.getString("val");
                status.setDefaultEncryption("ON".equalsIgnoreCase(val) || "1".equals(val));
            }
        } catch (SQLException ignored) {
            // MySQL < 8.0.16 不支持此变量
        }

        // 3. 列出已加密的表
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                             "WHERE CREATE_OPTIONS LIKE '%ENCRYPTION%'")) {
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("TABLE_SCHEMA") + "." + rs.getString("TABLE_NAME"));
            }
            status.setEncryptedTables(tables);
            status.setEncryptedTableCount(tables.size());
        }

        // 4. 检查 InnoDB 加密变量
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE '%encrypt%'")) {
            List<String> vars = new ArrayList<>();
            while (rs.next()) {
                vars.add(rs.getString(1) + " = " + rs.getString(2));
            }
            status.setEncryptionVariables(vars);
        }

        status.setEnabled(status.isKeyringLoaded() || status.getEncryptedTableCount() > 0);
    }

    /**
     * 检测 PostgreSQL 加密配置。
     */
    private static void checkPostgreSQL(Connection conn, TdeStatus status) throws SQLException {
        // 检查 pgcrypto 扩展
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT extname, extversion FROM pg_extension WHERE extname = 'pgcrypto'")) {
            if (rs.next()) {
                status.setKeyringPlugin("pgcrypto " + rs.getString("extversion"));
                status.setKeyringLoaded(true);
            }
        }

        // 检查 SSL 配置
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW ssl")) {
            if (rs.next()) {
                String sslStatus = rs.getString(1);
                status.setDefaultEncryption("on".equalsIgnoreCase(sslStatus));
            }
        }

        status.setEnabled(status.isKeyringLoaded());
        status.setMessage("PostgreSQL 不原生支持 TDE，建议使用文件系统级加密（LUKS）或 pgcrypto 扩展");
    }

    /**
     * 检测达梦数据库 TDE 配置。
     */
    private static void checkDM(Connection conn, TdeStatus status) throws SQLException {
        // 检查加密表空间
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TABLESPACE_NAME FROM DBA_TABLESPACES WHERE ENCRYPTED = 'YES'")) {
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("TABLESPACE_NAME"));
            }
            status.setEncryptedTables(tables);
            status.setEncryptedTableCount(tables.size());
        } catch (SQLException ignored) {
            // 视图可能不存在
        }

        // 检查列级加密
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TABLE_NAME, COLUMN_NAME FROM DBA_ENCRYPTED_COLUMNS")) {
            List<String> encCols = new ArrayList<>();
            while (rs.next()) {
                encCols.add(rs.getString("TABLE_NAME") + "." + rs.getString("COLUMN_NAME"));
            }
            if (!encCols.isEmpty()) {
                status.setEncryptionVariables(encCols);
            }
        } catch (SQLException ignored) {
            // 视图可能不存在
        }

        status.setEnabled(status.getEncryptedTableCount() > 0);
    }

    /**
     * TDE 状态信息。
     */
    @Data
    public static class TdeStatus {
        /** 数据库类型 */
        private String databaseType;
        /** TDE 是否已启用 */
        private boolean enabled;
        /** 密钥环插件信息 */
        private String keyringPlugin;
        /** 密钥环是否已加载 */
        private boolean keyringLoaded;
        /** 默认表空间加密是否开启 */
        private boolean defaultEncryption;
        /** 已加密的表列表 */
        private List<String> encryptedTables = new ArrayList<>();
        /** 已加密的表数量 */
        private int encryptedTableCount;
        /** 加密相关变量 */
        private List<String> encryptionVariables = new ArrayList<>();
        /** 提示信息 */
        private String message;
    }
}

