-- Forgex 基础信息与接口平台修复脚本
-- 目标：
-- 1. 基础信息模块只保留“基础信息主页”，去掉“基础信息”目录
-- 2. 物料管理恢复为一个页面，不再拆分“原材料 / 半成品 / 成品”菜单
-- 3. 修复基础信息数据旧租户 ID，恢复计量单位、物料等数据可见
-- 4. 修复接口平台数据旧租户 ID，恢复第三方系统、接口配置、调用记录可见
-- 备份：
--   D:\mine_product\forgex\sql_fix\forgex_admin_backup_before_basic_integration_20260516.sql
--   D:\mine_product\forgex\sql_fix\forgex_integration_backup_20260516.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1. admin 库：菜单结构收敛
-- =========================
USE `forgex_admin`;
START TRANSACTION;

-- 去掉“基础信息”目录，只保留“基础信息主页”
DELETE FROM `sys_role_menu`
WHERE `menu_id` = 209;

UPDATE `sys_menu`
SET `deleted` = 1,
    `status` = 0,
    `visible` = 0
WHERE `id` = 209;

-- 物料管理是一个页面，不再显示原材料、半成品、成品三个子菜单
DELETE FROM `sys_role_menu`
WHERE `menu_id` IN (263, 264, 265, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284);

UPDATE `sys_menu`
SET `deleted` = 1,
    `status` = 0,
    `visible` = 0
WHERE `id` IN (263, 264, 265, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284);

-- 保留物料管理主页面和基础信息主页
UPDATE `sys_menu`
SET `deleted` = 0,
    `status` = 1,
    `visible` = 1
WHERE `id` IN (208, 214, 215);

INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`)
SELECT 1, 1, m.id
FROM `sys_menu` m
WHERE m.id IN (208, 214, 215)
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.tenant_id = 1
      AND rm.role_id = 1
      AND rm.menu_id = m.id
  );

-- =========================
-- 2. admin 库：基础信息业务数据租户映射
--    旧租户 1993479636925403138 -> 当前租户 1
--    旧租户 1993479636925403140 -> 当前租户 2
-- =========================
UPDATE `basic_customer` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_customer_contact` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_customer_extra` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_customer_invoice` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_factory` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_barcode_record` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_field` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_print_config` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_print_exception` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_print_record` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_template` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_template_binding` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_template_detail` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_label_type` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_material` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_material_extend` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_material_extend_config` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_material_extend_schema` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_material_finished_goods` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_material_raw_material` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_material_semi_finished` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_packaging_type` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_supplier` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_supplier_contact` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_supplier_detail` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_supplier_qualification` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_unit` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_unit_conversion` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `basic_unit_type` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;

SET @next_role_menu := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_role_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_role_menu` AUTO_INCREMENT = ', @next_role_menu);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

COMMIT;

-- =========================
-- 3. integration 库：接口平台业务数据租户映射
-- =========================
USE `forgex_integration`;
START TRANSACTION;

UPDATE `fx_api_call_log_202604` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_api_call_log_202605` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_api_call_log_202606` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_api_config` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_api_outbound_target` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_api_param_config` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_api_param_mapping` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_api_task` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_api_task_result` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_third_authorization` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;
UPDATE `fx_third_system` SET `tenant_id` = CASE WHEN `tenant_id` = 1993479636925403138 THEN 1 WHEN `tenant_id` = 1993479636925403140 THEN 2 ELSE `tenant_id` END;

COMMIT;

-- =========================
-- 4. 校验
-- =========================
USE `forgex_admin`;
SELECT 'active_basic_info_catalog' AS check_name, COUNT(*) AS cnt
FROM `sys_menu`
WHERE `id` = 209 AND `deleted` = 0;

SELECT 'active_material_children' AS check_name, COUNT(*) AS cnt
FROM `sys_menu`
WHERE `id` IN (263, 264, 265)
  AND `deleted` = 0;

SELECT 'basic_unit_type' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `basic_unit_type`
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT 'basic_unit' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `basic_unit`
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT 'basic_material' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `basic_material`
GROUP BY tenant_id
ORDER BY tenant_id;

USE `forgex_integration`;
SELECT 'fx_third_system' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `fx_third_system`
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT 'fx_api_config' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `fx_api_config`
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT 'fx_api_call_log_202605' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `fx_api_call_log_202605`
GROUP BY tenant_id
ORDER BY tenant_id;

SET FOREIGN_KEY_CHECKS = 1;
