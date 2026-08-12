-- Roll back workflow pending action button permissions seeded by
-- 20260812_workflow_pending_action_permissions.sql.

SET NAMES utf8mb4;
USE `forgex_admin`;

DELETE role_menu
FROM `sys_role_menu` role_menu
JOIN `sys_menu` button ON button.`id` = role_menu.`menu_id`
JOIN `sys_menu` parent ON parent.`id` = button.`parent_id`
WHERE parent.`component_key` = 'ApprovalMyPending'
  AND button.`perm_key` IN (
    'wf:execution:addSign',
    'wf:execution:transfer',
    'wf:execution:delegate'
  )
  AND button.`create_by` = '20260812_workflow_pending_action_permissions';

DELETE button
FROM `sys_menu` button
JOIN `sys_menu` parent ON parent.`id` = button.`parent_id`
WHERE parent.`component_key` = 'ApprovalMyPending'
  AND button.`perm_key` IN (
    'wf:execution:addSign',
    'wf:execution:transfer',
    'wf:execution:delegate'
  )
  AND button.`create_by` = '20260812_workflow_pending_action_permissions';
