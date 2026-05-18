-- Forgex 老菜单恢复脚本
-- 说明：
-- 1. 恢复 sys_menu 中仍处于软删除状态的老菜单/按钮
-- 2. 补齐租户 1 系统管理员角色的菜单授权
-- 3. 执行前已完成备份：D:\mine_product\forgex\sql_fix\forgex_admin_backup_20260516.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `forgex_admin`;
START TRANSACTION;

-- 1) 恢复被软删除的菜单、目录、按钮
UPDATE `sys_menu`
SET `deleted` = 0,
    `status` = 1,
    `visible` = CASE
        WHEN `type` IN ('catalog', 'menu') THEN 1
        ELSE `visible`
    END
WHERE `deleted` = 1;

-- 2) 补齐租户 1 系统管理员角色的菜单授权
INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`)
SELECT m.tenant_id, r.id, m.id
FROM `sys_menu` m
JOIN `sys_role` r
  ON r.tenant_id = m.tenant_id
 AND r.role_key = 'admin'
 AND r.deleted = 0
WHERE m.tenant_id = 1
  AND m.deleted = 0
  AND m.status = 1
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.tenant_id = m.tenant_id
      AND rm.role_id = r.id
      AND rm.menu_id = m.id
  );

-- 3) 同步自增值
SET @next_menu := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_menu` AUTO_INCREMENT = ', @next_menu);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_role_menu := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_role_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_role_menu` AUTO_INCREMENT = ', @next_role_menu);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) 校验输出
SELECT 'sys_menu_deleted' AS check_name, COUNT(*) AS cnt
FROM `sys_menu`
WHERE `deleted` = 1;

SELECT 'tenant1_admin_missing_menu' AS check_name, COUNT(*) AS cnt
FROM `sys_menu` m
JOIN `sys_role` r
  ON r.tenant_id = m.tenant_id
 AND r.role_key = 'admin'
 AND r.deleted = 0
LEFT JOIN `sys_role_menu` rm
  ON rm.tenant_id = m.tenant_id
 AND rm.role_id = r.id
 AND rm.menu_id = m.id
WHERE m.tenant_id = 1
  AND m.deleted = 0
  AND m.status = 1
  AND rm.id IS NULL;

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
