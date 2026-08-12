-- Workflow pending action button permission seed.
-- Target database: forgex_admin.
-- Idempotent: safe to execute multiple times.

SET NAMES utf8mb4;
USE `forgex_admin`;

SET @now := NOW();
SET @script_user := '20260812_workflow_pending_action_permissions';

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT parent.`tenant_id`, parent.`tenant_type`, parent.`module_id`, parent.`id`, 'button', seed.`path`, seed.`name`,
       seed.`name_i18n_json`, NULL, NULL, seed.`perm_key`, seed.`order_num`, 1, 1,
       @now, @script_user, @now, @script_user, 0, parent.`menu_level` + 2, 'embedded', NULL
FROM (
  SELECT 'addSign' path, '加签审批' name,
         JSON_OBJECT('zh-CN','加签审批','zh-TW','加簽審批','en-US','Add Sign','ja-JP','承認者追加','ko-KR','결재자 추가') name_i18n_json,
         'wf:execution:addSign' perm_key, 3 order_num
  UNION ALL
  SELECT 'transfer', '转交审批',
         JSON_OBJECT('zh-CN','转交审批','zh-TW','轉交審批','en-US','Transfer','ja-JP','承認転送','ko-KR','결재 이관'),
         'wf:execution:transfer', 4
  UNION ALL
  SELECT 'delegate', '委托审批',
         JSON_OBJECT('zh-CN','委托审批','zh-TW','委託審批','en-US','Delegate','ja-JP','承認委任','ko-KR','결재 위임'),
         'wf:execution:delegate', 5
) seed
JOIN `sys_menu` parent
  ON parent.`deleted` = 0
 AND parent.`component_key` = 'ApprovalMyPending'
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu` existing
  WHERE existing.`deleted` = 0
    AND existing.`parent_id` = parent.`id`
    AND existing.`perm_key` = seed.`perm_key`
);

INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT DISTINCT action_role.`tenant_id`, action_role.`role_id`, button.`id`
FROM `sys_menu` parent
JOIN `sys_menu` existing_action
  ON existing_action.`tenant_id` = parent.`tenant_id`
 AND existing_action.`parent_id` = parent.`id`
 AND existing_action.`deleted` = 0
 AND existing_action.`perm_key` IN ('wf:execution:approve', 'wf:execution:reject')
JOIN `sys_role_menu` action_role
  ON action_role.`tenant_id` = existing_action.`tenant_id`
 AND action_role.`menu_id` = existing_action.`id`
JOIN `sys_menu` button
  ON button.`tenant_id` = parent.`tenant_id`
 AND button.`parent_id` = parent.`id`
 AND button.`deleted` = 0
 AND button.`create_by` = @script_user
WHERE parent.`deleted` = 0
  AND parent.`component_key` = 'ApprovalMyPending'
  AND button.`perm_key` IN (
    'wf:execution:addSign',
    'wf:execution:transfer',
    'wf:execution:delegate'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` existing
    WHERE existing.`tenant_id` = action_role.`tenant_id`
      AND existing.`role_id` = action_role.`role_id`
      AND existing.`menu_id` = button.`id`
  );
