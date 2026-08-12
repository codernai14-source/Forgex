-- 用户直属上级字段升级脚本
-- 适用库：forgex_admin
-- 说明：幂等补齐 sys_user.superior_user_id，修复登录选择租户后加载路由时的字段不存在异常。

USE `forgex_admin`;

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'superior_user_id'
);

SET @add_column_sql := IF(
    @column_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `superior_user_id` bigint NULL DEFAULT NULL COMMENT ''直属上级用户ID'' AFTER `employee_id`',
    'SELECT 1'
);
PREPARE add_column_stmt FROM @add_column_sql;
EXECUTE add_column_stmt;
DEALLOCATE PREPARE add_column_stmt;

SET @index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND INDEX_NAME = 'idx_superior_user_id'
);

SET @add_index_sql := IF(
    @index_exists = 0,
    'ALTER TABLE `sys_user` ADD INDEX `idx_superior_user_id` (`superior_user_id` ASC)',
    'SELECT 1'
);
PREPARE add_index_stmt FROM @add_index_sql;
EXECUTE add_index_stmt;
DEALLOCATE PREPARE add_index_stmt;

-- 验证字段和索引已存在。
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_user'
  AND COLUMN_NAME = 'superior_user_id';

SELECT INDEX_NAME, COLUMN_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_user'
  AND INDEX_NAME = 'idx_superior_user_id';
