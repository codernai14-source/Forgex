-- Forgex 导入/导出配置、定时任务、首页组件表格修复脚本
-- 执行前已存在备份：
--   D:\mine_product\forgex\sql_fix\forgex_common_backup_before_excel_homepage_20260516.sql
--   D:\mine_product\forgex\sql_fix\forgex_job_backup_20260516.sql
--
-- 修复内容：
--   1. 将 forgex_common 中导入/导出配置及对应动态表格配置的旧租户 ID 映射到当前租户。
--   2. 将 forgex_job 中定时任务相关表的旧租户 ID 映射到当前租户。
--   3. 补齐首页组件目录 SystemHomepageComponentTable 的公共动态表格配置，解决“有总数但行内容空白”的问题。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1. common 库：导入/导出配置租户映射
-- =========================
USE `forgex_common`;
START TRANSACTION;

UPDATE `fx_excel_import_config`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END
WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);

UPDATE `fx_excel_import_config_item`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END
WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);

UPDATE `fx_excel_export_config`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END
WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);

UPDATE `fx_excel_export_config_item`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END
WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);

UPDATE `fx_table_config`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END
WHERE `table_code` IN ('ExcelImportConfigTable', 'ExcelExportConfigTable')
  AND `tenant_id` IN (1993479636925403138, 1993479636925403140);

UPDATE `fx_table_column_config`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END
WHERE `table_code` IN ('ExcelImportConfigTable', 'ExcelExportConfigTable')
  AND `tenant_id` IN (1993479636925403138, 1993479636925403140);

-- =========================
-- 2. common 库：首页组件目录动态表格配置
-- =========================
INSERT INTO `fx_table_config` (
    `tenant_id`, `table_code`, `table_name_i18n_json`, `table_type`, `row_key`,
    `default_page_size`, `default_sort_json`, `enabled`, `version`, `create_by`, `update_by`, `deleted`
)
SELECT 0,
       'SystemHomepageComponentTable',
       JSON_OBJECT('zh-CN', '首页组件目录', 'zh-TW', '首頁組件目錄', 'en-US', 'Homepage Components'),
       'NORMAL',
       'id',
       20,
       JSON_OBJECT('field', 'orderNum', 'order', 'asc'),
       1,
       1,
       '20260516_fix_excel_job_homepage_table',
       '20260516_fix_excel_job_homepage_table',
       0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_config`
    WHERE `tenant_id` = 0
      AND `table_code` = 'SystemHomepageComponentTable'
      AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'componentName',
       JSON_OBJECT('zh-CN', '组件名称', 'zh-TW', '組件名稱', 'en-US', 'Component Name'),
       'left', 180, NULL, 1, 1, NULL, 1, 'input', 'like', NULL, NULL, NULL, 1, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'componentName' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'componentCode',
       JSON_OBJECT('zh-CN', '组件编码', 'zh-TW', '組件編碼', 'en-US', 'Component Code'),
       'left', 180, NULL, 1, 1, NULL, 1, 'input', 'like', NULL, NULL, NULL, 2, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'componentCode' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'categoryName',
       JSON_OBJECT('zh-CN', '分类名称', 'zh-TW', '分類名稱', 'en-US', 'Category'),
       'left', 140, NULL, 1, 1, NULL, 1, 'input', 'like', NULL, 'tag', NULL, 3, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'categoryName' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'moduleCode',
       JSON_OBJECT('zh-CN', '模块编码', 'zh-TW', '模組編碼', 'en-US', 'Module Code'),
       'center', 120, NULL, 0, 1, NULL, 1, 'input', 'like', NULL, NULL, NULL, 4, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'moduleCode' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'scopeLevel',
       JSON_OBJECT('zh-CN', '作用范围', 'zh-TW', '作用範圍', 'en-US', 'Scope'),
       'center', 110, NULL, 0, 1, NULL, 0, NULL, NULL, NULL, 'tag', NULL, 5, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'scopeLevel' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'componentPath',
       JSON_OBJECT('zh-CN', '组件路径', 'zh-TW', '組件路徑', 'en-US', 'Component Path'),
       'left', 180, NULL, 1, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 6, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'componentPath' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'enabled',
       JSON_OBJECT('zh-CN', '启用状态', 'zh-TW', '啟用狀態', 'en-US', 'Enabled'),
       'center', 100, NULL, 0, 1, NULL, 1, 'select', 'eq', NULL, 'tag', NULL, 7, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'enabled' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'orderNum',
       JSON_OBJECT('zh-CN', '排序', 'zh-TW', '排序', 'en-US', 'Order'),
       'center', 90, NULL, 0, 1, NULL, 0, NULL, NULL, NULL, NULL, NULL, 8, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'orderNum' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config` (
    `tenant_id`, `table_code`, `field`, `title_i18n_json`, `align`, `width`,
    `fixed`, `ellipsis`, `sortable`, `sorter_field`, `queryable`, `query_type`,
    `query_operator`, `dict_code`, `render_type`, `perm_key`, `order_num`, `enabled`,
    `create_by`, `update_by`, `deleted`
)
SELECT 0, 'SystemHomepageComponentTable', 'action',
       JSON_OBJECT('zh-CN', '操作', 'zh-TW', '操作', 'en-US', 'Action'),
       'center', 160, 'right', 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 99, 1,
       '20260516_fix_excel_job_homepage_table', '20260516_fix_excel_job_homepage_table', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config`
    WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = 'action' AND `deleted` = 0
);

SET @next_table_config := (SELECT COALESCE(MAX(id), 0) + 1 FROM `fx_table_config`);
SET @sql := CONCAT('ALTER TABLE `fx_table_config` AUTO_INCREMENT = ', @next_table_config);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_table_col_config := (SELECT COALESCE(MAX(id), 0) + 1 FROM `fx_table_column_config`);
SET @sql := CONCAT('ALTER TABLE `fx_table_column_config` AUTO_INCREMENT = ', @next_table_col_config);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

COMMIT;

-- =========================
-- 3. job 库：定时任务租户映射
-- =========================
USE `forgex_job`;
START TRANSACTION;

UPDATE `sys_job` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_alarm_log` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_alarm_rule` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_instance` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_log` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_retry` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_task` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_workflow` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_workflow_edge` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_workflow_execution` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);
UPDATE `sys_job_workflow_node` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END WHERE `tenant_id` IN (1993479636925403138, 1993479636925403140);

SET @next_job_task := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_job_task`);
SET @sql := CONCAT('ALTER TABLE `sys_job_task` AUTO_INCREMENT = ', @next_job_task);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- 4. 校验结果
-- =========================
USE `forgex_common`;
SELECT 'import_config' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `fx_excel_import_config`
WHERE `deleted` = 0
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT 'export_config' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `fx_excel_export_config`
WHERE `deleted` = 0
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT 'homepage_table_config' AS check_name, tenant_id, table_code, COUNT(*) AS cnt
FROM `fx_table_config`
WHERE `table_code` = 'SystemHomepageComponentTable'
  AND `deleted` = 0
GROUP BY tenant_id, table_code
ORDER BY tenant_id;

SELECT 'homepage_table_columns' AS check_name, tenant_id, table_code, COUNT(*) AS cnt
FROM `fx_table_column_config`
WHERE `table_code` = 'SystemHomepageComponentTable'
  AND `deleted` = 0
  AND `enabled` = 1
GROUP BY tenant_id, table_code
ORDER BY tenant_id;

USE `forgex_job`;
SELECT 'job_task' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `sys_job_task`
WHERE `deleted` = 0
GROUP BY tenant_id
ORDER BY tenant_id;
