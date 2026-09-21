-- 目标数据库：forgex_admin；兼容 MySQL 8.0。
-- 仅在同步回退使用 sort_order 的应用代码后执行；删除字段会丢失已有排序值。
-- 执行前备份该字段；若升级前字段已经存在，不应执行本回滚。
USE `forgex_admin`;
SET NAMES utf8mb4;

SET @workshop_sort_sql := (
    SELECT IF(COUNT(*) > 0,
        'ALTER TABLE `basic_workshop` DROP COLUMN `sort_order`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'basic_workshop'
      AND COLUMN_NAME = 'sort_order'
);
PREPARE workshop_sort_stmt FROM @workshop_sort_sql;
EXECUTE workshop_sort_stmt;
DEALLOCATE PREPARE workshop_sort_stmt;
