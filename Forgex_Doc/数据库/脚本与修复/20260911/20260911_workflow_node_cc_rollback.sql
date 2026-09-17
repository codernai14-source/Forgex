-- 回滚审批节点独立抄送：删除运行时表、配置表、节点开关、菜单权限、消息模板、动态表。
-- 目标库：forgex_workflow、forgex_admin、forgex_common。
-- 请使用 UTF-8 / utf8mb4 客户端执行。

SET NAMES utf8mb4;

USE `forgex_common`;

UPDATE `fx_table_column_config`
SET `deleted` = 1
WHERE `table_code` = 'WfMyCcTaskTable'
  AND `deleted` = 0;

UPDATE `fx_table_config`
SET `deleted` = 1
WHERE `table_code` = 'WfMyCcTaskTable'
  AND `deleted` = 0;

USE `forgex_admin`;

UPDATE `sys_message_template_content` c
JOIN `sys_message_template` t ON t.id = c.template_id
SET c.deleted = 1
WHERE t.template_code = 'WF_CC'
  AND c.deleted = 0;

UPDATE `sys_message_template`
SET `deleted` = 1
WHERE `template_code` = 'WF_CC'
  AND `deleted` = 0;

DELETE rm
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.id = rm.menu_id
WHERE m.component_key = 'ApprovalMyCc';

DELETE rp
FROM `sys_role_permission` rp
JOIN `sys_permission` p ON p.id = rp.permission_id
WHERE p.permission_key = 'wf:myTask:cc';

UPDATE `sys_permission`
SET `deleted` = 1
WHERE `permission_key` = 'wf:myTask:cc'
  AND `deleted` = 0;

UPDATE `sys_menu`
SET `deleted` = 1
WHERE `component_key` = 'ApprovalMyCc'
  AND `deleted` = 0;

USE `forgex_workflow`;

DROP TABLE IF EXISTS `wf_task_cc_record`;
DROP TABLE IF EXISTS `wf_task_node_cc`;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'forgex_workflow'
    AND TABLE_NAME = 'wf_task_node_config'
    AND COLUMN_NAME = 'cc_enabled'
);
SET @sql := IF(@col_exists > 0,
  'ALTER TABLE `wf_task_node_config` DROP COLUMN `cc_enabled`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
