-- ============================================================
-- 多租户配置管理优化 - 数据库迁移脚本
-- 执行日期：2026-04-03
-- 说明：为租户层级关系和菜单适用租户类型提供数据库支持
-- 用途：当前脚本用于开发 / 测试环境，可重复执行
-- 注意：执行前请先确认当前连接数据库为目标库
-- ============================================================

SET @current_db = DATABASE();
SELECT CONCAT('Current database: ', @current_db) AS message;

-- ========================================
-- 1. sys_tenant 增加 parent_tenant_id 字段
-- ========================================
SELECT COUNT(*)
INTO @tenant_parent_column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @current_db
  AND TABLE_NAME = 'sys_tenant'
  AND COLUMN_NAME = 'parent_tenant_id';

SET @sql = IF(
  @tenant_parent_column_exists = 0,
  "ALTER TABLE sys_tenant ADD COLUMN parent_tenant_id BIGINT NULL COMMENT 'Parent tenant ID; NULL means main or top-level tenant' AFTER tenant_type",
  "SELECT 'Column sys_tenant.parent_tenant_id already exists' AS message"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*)
INTO @tenant_parent_index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = @current_db
  AND TABLE_NAME = 'sys_tenant'
  AND INDEX_NAME = 'idx_parent_tenant_id';

SET @sql = IF(
  @tenant_parent_index_exists = 0,
  "ALTER TABLE sys_tenant ADD INDEX idx_parent_tenant_id (parent_tenant_id)",
  "SELECT 'Index idx_parent_tenant_id already exists' AS message"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 2. sys_menu 增加 tenant_type 字段
-- ========================================
SELECT COUNT(*)
INTO @menu_tenant_type_column_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @current_db
  AND TABLE_NAME = 'sys_menu'
  AND COLUMN_NAME = 'tenant_type';

SET @sql = IF(
  @menu_tenant_type_column_exists = 0,
  "ALTER TABLE sys_menu ADD COLUMN tenant_type VARCHAR(50) NULL DEFAULT 'PUBLIC' COMMENT 'Applicable tenant type: MAIN_TENANT/CUSTOMER_TENANT/SUPPLIER_TENANT/PARTNER_TENANT/PUBLIC; PUBLIC means all tenant types' AFTER tenant_id",
  "ALTER TABLE sys_menu MODIFY COLUMN tenant_type VARCHAR(50) NULL DEFAULT 'PUBLIC' COMMENT 'Applicable tenant type: MAIN_TENANT/CUSTOMER_TENANT/SUPPLIER_TENANT/PARTNER_TENANT/PUBLIC; PUBLIC means all tenant types' AFTER tenant_id"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*)
INTO @menu_tenant_type_index_exists
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = @current_db
  AND TABLE_NAME = 'sys_menu'
  AND INDEX_NAME = 'idx_tenant_type';

SET @sql = IF(
  @menu_tenant_type_index_exists = 0,
  "ALTER TABLE sys_menu ADD INDEX idx_tenant_type (tenant_type)",
  "SELECT 'Index idx_tenant_type already exists' AS message"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 3. 数据初始化
-- ========================================
-- 将主租户下尚未设置 tenant_type 的菜单统一标记为 PUBLIC
UPDATE sys_menu m
JOIN sys_tenant t ON t.id = m.tenant_id
SET m.tenant_type = 'PUBLIC'
WHERE t.tenant_type = 'MAIN_TENANT'
  AND (m.tenant_type IS NULL OR TRIM(m.tenant_type) = '');

SELECT ROW_COUNT() AS updated_menu_rows;

-- ========================================
-- 4. 验证查询
-- ========================================
SHOW COLUMNS FROM sys_tenant LIKE 'parent_tenant_id';
SHOW INDEX FROM sys_tenant WHERE Key_name = 'idx_parent_tenant_id';

SHOW COLUMNS FROM sys_menu LIKE 'tenant_type';
SHOW INDEX FROM sys_menu WHERE Key_name = 'idx_tenant_type';

SELECT id, tenant_name, tenant_type, parent_tenant_id
FROM sys_tenant
ORDER BY id
LIMIT 10;

SELECT tenant_type, COUNT(*) AS cnt
FROM sys_menu
GROUP BY tenant_type
ORDER BY tenant_type;
