-- Forgex 老数据恢复脚本
-- 说明：
-- 1. 恢复被软删除的核心组织数据
-- 2. 补齐系统管理员角色遗漏的菜单授权
-- 3. 修正历史导出中已经乱码的部门/职位名称
-- 4. 执行前已完成备份：D:\mine_product\forgex\sql_fix\forgex_admin_backup_20260516.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `forgex_admin`;
START TRANSACTION;

-- 1) 恢复老用户
UPDATE `sys_user`
SET `deleted` = 0,
    `status` = 1
WHERE `id` = 4;

-- 2) 恢复老部门数据
UPDATE `sys_department`
SET `deleted` = 0,
    `status` = 1,
    `dept_name` = '平台部门'
WHERE `id` = 18;

UPDATE `sys_department`
SET `deleted` = 0,
    `status` = 1
WHERE `id` = 20;

-- 3) 恢复老职位数据
UPDATE `sys_position`
SET `deleted` = 0,
    `status` = 1,
    `position_name` = '平台管理员'
WHERE `id` = 11;

-- 4) 补齐系统管理员角色的菜单权限
--    这几个菜单在当前库里是启用状态，但系统管理员角色缺少授权链路。
INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`)
SELECT 1, 1, m.id
FROM `sys_menu` m
WHERE m.tenant_id = 1
  AND m.deleted = 0
  AND m.status = 1
  AND m.id IN (9, 10, 11, 12)
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.tenant_id = 1
      AND rm.role_id = 1
      AND rm.menu_id = m.id
  );

-- 5) 恢复老测试用户与部门/职位的关联显示
UPDATE `sys_user`
SET `department_id` = 18
WHERE `id` = 4
  AND (`department_id` IS NULL OR `department_id` = 1);

-- 6) 同步自增值，避免后续新增再撞旧数据
SET @next_user := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_user`);
SET @sql := CONCAT('ALTER TABLE `sys_user` AUTO_INCREMENT = ', @next_user);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_dept := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_department`);
SET @sql := CONCAT('ALTER TABLE `sys_department` AUTO_INCREMENT = ', @next_dept);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_pos := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_position`);
SET @sql := CONCAT('ALTER TABLE `sys_position` AUTO_INCREMENT = ', @next_pos);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @next_role_menu := (SELECT COALESCE(MAX(id), 0) + 1 FROM `sys_role_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_role_menu` AUTO_INCREMENT = ', @next_role_menu);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 7) 校验输出
SELECT 'sys_user' AS tbl, id, account, username, deleted, status, tenant_id, department_id
FROM `sys_user`
WHERE `id` IN (4)
ORDER BY id;

SELECT 'sys_department' AS tbl, id, dept_name, dept_code, deleted, status, tenant_id
FROM `sys_department`
WHERE `id` IN (18, 20)
ORDER BY id;

SELECT 'sys_position' AS tbl, id, position_name, position_code, department_id, deleted, status, tenant_id
FROM `sys_position`
WHERE `id` IN (11)
ORDER BY id;

SELECT 'sys_role_menu' AS tbl, tenant_id, role_id, menu_id
FROM `sys_role_menu`
WHERE tenant_id = 1 AND role_id = 1 AND menu_id IN (9, 10, 11, 12)
ORDER BY menu_id;

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
