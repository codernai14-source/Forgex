-- 目标数据库：forgex_admin；兼容 MySQL 8.0。
-- 修复 BasicWorkshop 查询排序字段不存在的问题；重复执行不会覆盖已有排序值。
USE `forgex_admin`;
SET NAMES utf8mb4;

SET @workshop_sort_sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE `basic_workshop` ADD COLUMN `sort_order` int NOT NULL DEFAULT 0 COMMENT ''排序号'' AFTER `workshop_name`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'basic_workshop'
      AND COLUMN_NAME = 'sort_order'
);
PREPARE workshop_sort_stmt FROM @workshop_sort_sql;
EXECUTE workshop_sort_stmt;
DEALLOCATE PREPARE workshop_sort_stmt;
