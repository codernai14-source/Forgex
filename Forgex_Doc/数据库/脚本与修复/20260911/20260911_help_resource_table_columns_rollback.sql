-- 回滚帮助资源动态表「格式」「大小」列。
-- 目标库：forgex_common。

SET NAMES utf8mb4;

USE `forgex_common`;

DELETE FROM `fx_table_column_config`
WHERE `table_code` = 'SystemHelpResourceTable'
  AND `field` IN ('fileExt', 'fileSize');

SELECT 'help_resource_table_columns_rollback_done' AS result;
