-- 回滚帮助中心专项脚本：删除菜单权限、配置、表格列和业务表。
-- 目标库：forgex_admin、forgex_common。
-- 请使用 UTF-8 / utf8mb4 客户端执行。

SET NAMES utf8mb4;

USE `forgex_admin`;

DELETE rm FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.id = rm.menu_id
WHERE m.component_key = 'SystemHelpResource'
   OR m.perm_key IN ('sys:help:view', 'sys:help:add', 'sys:help:edit', 'sys:help:delete', 'sys:help:contact:edit');

DELETE rp FROM `sys_role_permission` rp
JOIN `sys_permission` p ON p.id = rp.permission_id
WHERE p.permission_key IN ('sys:help:view', 'sys:help:add', 'sys:help:edit', 'sys:help:delete', 'sys:help:contact:edit');

DELETE FROM `sys_permission`
WHERE permission_key IN ('sys:help:view', 'sys:help:add', 'sys:help:edit', 'sys:help:delete', 'sys:help:contact:edit');

DELETE FROM `sys_menu`
WHERE perm_key IN ('sys:help:view', 'sys:help:add', 'sys:help:edit', 'sys:help:delete', 'sys:help:contact:edit')
   OR component_key = 'SystemHelpResource';

DROP TABLE IF EXISTS `sys_help_resource`;

USE `forgex_common`;

DELETE FROM `fx_table_column_config` WHERE table_code = 'SystemHelpResourceTable';
DELETE FROM `fx_table_config` WHERE table_code = 'SystemHelpResourceTable';
DELETE FROM `sys_config` WHERE config_key IN ('system.help.contact', 'system.help.upload');

SELECT 'sys_help_resource_rollback_done' AS result;
