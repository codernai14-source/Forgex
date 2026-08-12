-- 用户直属上级字段回滚脚本
-- 适用库：forgex_admin
-- 说明：仅在确认已回退应用代码且不再使用直属上级关系时执行。

USE `forgex_admin`;

SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'superior_user_id'
);

SET @drop_column_sql := IF(
    @column_exists = 1,
    'ALTER TABLE `sys_user` DROP COLUMN `superior_user_id`',
    'SELECT 1'
);
PREPARE drop_column_stmt FROM @drop_column_sql;
EXECUTE drop_column_stmt;
DEALLOCATE PREPARE drop_column_stmt;
