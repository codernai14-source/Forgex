-- Forgex 审批模块修复脚本
-- 目标：
-- 1. 修复审批工作台重复菜单
-- 2. 将工作流审批配置从旧租户 ID 映射到当前租户
-- 3. 让审批任务配置、节点配置、待办和历史记录重新可见
-- 备份：
--   admin 备份：D:\mine_product\forgex\sql_fix\forgex_admin_backup_20260516.sql
--   workflow 备份：D:\mine_product\forgex\sql_fix\forgex_workflow_backup_20260516.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1. admin 库：删除重复的审批工作台菜单
--    保留 id=9，移除更晚插入的重复菜单 id=195
-- =========================
USE `forgex_admin`;
START TRANSACTION;

DELETE FROM `sys_role_menu`
WHERE `tenant_id` = 1
  AND `role_id` = 1
  AND `menu_id` = 195;

UPDATE `sys_menu`
SET `deleted` = 1,
    `status` = 0
WHERE `id` = 195
  AND `tenant_id` = 1;

-- 保持保留菜单可见
UPDATE `sys_menu`
SET `deleted` = 0,
    `status` = 1,
    `visible` = 1
WHERE `id` = 9
  AND `tenant_id` = 1;

SET @next_menu := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_menu` AUTO_INCREMENT = ', @next_menu);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_role_menu := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_role_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_role_menu` AUTO_INCREMENT = ', @next_role_menu);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =========================
-- 2. workflow 库：把旧租户数据映射到当前租户
--    旧租户 1993479636925403138 -> 当前租户 1
--    旧租户 1993479636925403140 -> 当前租户 2
-- =========================
COMMIT;

USE `forgex_workflow`;
START TRANSACTION;

UPDATE `wf_task_config`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_task_node_config`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_task_node_approver`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_task_node_rule`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_task_execution`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_task_execution_detail`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_task_execution_approver`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_my_task`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_task_approval_action_log`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

UPDATE `wf_task_approval_instance`
SET `tenant_id` = CASE
        WHEN `tenant_id` = 1993479636925403138 THEN 1
        WHEN `tenant_id` = 1993479636925403140 THEN 2
        ELSE `tenant_id`
    END;

-- message template 维持公共模板，不改成租户私有，避免影响其他租户复用
-- 但保留当前数据，方便工作流模块按全局模板读取

SET @next_wf_task_config := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_config`);
SET @sql := CONCAT('ALTER TABLE `wf_task_config` AUTO_INCREMENT = ', @next_wf_task_config);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_node_config := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_node_config`);
SET @sql := CONCAT('ALTER TABLE `wf_task_node_config` AUTO_INCREMENT = ', @next_wf_node_config);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_node_approver := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_node_approver`);
SET @sql := CONCAT('ALTER TABLE `wf_task_node_approver` AUTO_INCREMENT = ', @next_wf_node_approver);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_node_rule := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_node_rule`);
SET @sql := CONCAT('ALTER TABLE `wf_task_node_rule` AUTO_INCREMENT = ', @next_wf_node_rule);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_execution := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_execution`);
SET @sql := CONCAT('ALTER TABLE `wf_task_execution` AUTO_INCREMENT = ', @next_wf_execution);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_execution_detail := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_execution_detail`);
SET @sql := CONCAT('ALTER TABLE `wf_task_execution_detail` AUTO_INCREMENT = ', @next_wf_execution_detail);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_execution_approver := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_execution_approver`);
SET @sql := CONCAT('ALTER TABLE `wf_task_execution_approver` AUTO_INCREMENT = ', @next_wf_execution_approver);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_my_task := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_my_task`);
SET @sql := CONCAT('ALTER TABLE `wf_my_task` AUTO_INCREMENT = ', @next_wf_my_task);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_action_log := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_approval_action_log`);
SET @sql := CONCAT('ALTER TABLE `wf_task_approval_action_log` AUTO_INCREMENT = ', @next_wf_action_log);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_wf_instance := (SELECT COALESCE(MAX(id), 0) + 1 FROM `wf_task_approval_instance`);
SET @sql := CONCAT('ALTER TABLE `wf_task_approval_instance` AUTO_INCREMENT = ', @next_wf_instance);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =========================
-- 3. 校验
-- =========================
COMMIT;

USE `forgex_admin`;
SELECT 'admin_menu_dupe' AS check_name, COUNT(*) AS cnt
FROM `sys_menu`
WHERE `name` = '审批工作台'
  AND `path` = 'dashboard'
  AND `tenant_id` = 1
  AND `deleted` = 0;

USE `forgex_workflow`;
SELECT 'wf_task_config' AS check_name, tenant_id, config_stage, COUNT(*) AS cnt
FROM `wf_task_config`
GROUP BY tenant_id, config_stage
ORDER BY tenant_id, config_stage;

SELECT 'wf_task_node_config' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `wf_task_node_config`
GROUP BY tenant_id
ORDER BY tenant_id;

SELECT 'wf_task_execution' AS check_name, tenant_id, COUNT(*) AS cnt
FROM `wf_task_execution`
GROUP BY tenant_id
ORDER BY tenant_id;

SET FOREIGN_KEY_CHECKS = 1;
