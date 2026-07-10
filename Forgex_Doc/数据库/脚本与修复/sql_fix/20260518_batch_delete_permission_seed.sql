-- Batch delete button permission seed.
-- Target database: forgex_admin.
-- Idempotent: safe to execute multiple times.
-- Scope: pages that already expose or receive batch delete UI in this upgrade.

SET NAMES utf8mb4;
USE `forgex_admin`;

SET @now := NOW();
SET @script_user := '20260518_batch_delete_permission_seed';

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT parent.`tenant_id`, parent.`tenant_type`, parent.`module_id`, parent.`id`, 'button', 'batchDelete', seed.`name`,
       JSON_OBJECT('zh-CN','批量删除','zh-TW','批量刪除','en-US','Batch Delete','ja-JP','一括削除','ko-KR','일괄 삭제'),
       NULL, NULL, seed.`perm_key`, seed.`order_num`, 0, 1, @now, @script_user, @now, @script_user, 0, parent.`menu_level` + 1, 'embedded', NULL
FROM (
  SELECT 'BasicEmployee' parent_key, '人员批量删除' name, 'basic:employee:batchDelete' perm_key, 8 order_num
  UNION ALL SELECT 'BasicShift', '班次批量删除', 'basic:shift:batchDelete', 5
  UNION ALL SELECT 'BasicTeam', '班组批量删除', 'basic:team:batchDelete', 5
  UNION ALL SELECT 'BasicWorkshop', '车间批量删除', 'basic:workshop:batchDelete', 5
  UNION ALL SELECT 'BasicMaterial', '物料批量删除', 'basic:material:batchDelete', 8
  UNION ALL SELECT 'BasicCustomer', '客户批量删除', 'basic:customer:batchDelete', 8
  UNION ALL SELECT 'BasicSupplier', '供应商批量删除', 'basic:supplier:batchDelete', 8
  UNION ALL SELECT 'LabelType', '标签类型批量删除', 'label:type:batchDelete', 5
  UNION ALL SELECT 'LabelField', '标签字段批量删除', 'label:field:batchDelete', 5
  UNION ALL SELECT 'LabelTemplate', '标签模板批量删除', 'label:template:batchDelete', 8
  UNION ALL SELECT 'LabelBinding', '标签绑定批量删除', 'label:binding:batchDelete', 5
) seed
JOIN `sys_menu` parent ON parent.`deleted` = 0 AND parent.`component_key` = seed.`parent_key`
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu` existing
  WHERE existing.`deleted` = 0
    AND existing.`parent_id` = parent.`id`
    AND existing.`perm_key` = seed.`perm_key`
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT seed.`permission_name`, seed.`permission_key`, seed.`url`, 'POST', 0, @now, @now, 0
FROM (
  SELECT '人员批量删除' permission_name, 'basic:employee:batchDelete' permission_key, '/basic/employee/batchDelete' url
  UNION ALL SELECT '班次批量删除', 'basic:shift:batchDelete', '/basic/shift/batchDelete'
  UNION ALL SELECT '班组批量删除', 'basic:team:batchDelete', '/basic/team/batchDelete'
  UNION ALL SELECT '车间批量删除', 'basic:workshop:batchDelete', '/basic/workshop/batchDelete'
  UNION ALL SELECT '物料批量删除', 'basic:material:batchDelete', '/basic/material/batchDelete'
  UNION ALL SELECT '客户批量删除', 'basic:customer:batchDelete', '/basic/customer/batchDelete'
  UNION ALL SELECT '供应商批量删除', 'basic:supplier:batchDelete', '/basic/supplier/batchDelete'
  UNION ALL SELECT '标签类型批量删除', 'label:type:batchDelete', '/basic/label/type/batchDelete'
  UNION ALL SELECT '标签字段批量删除', 'label:field:batchDelete', '/basic/label/field/batchDelete'
  UNION ALL SELECT '标签模板批量删除', 'label:template:batchDelete', '/basic/label/template/batchDelete'
  UNION ALL SELECT '标签绑定批量删除', 'label:binding:batchDelete', '/basic/label/binding/batchDelete'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p
  WHERE p.`deleted` = 0 AND p.`permission_key` = seed.`permission_key`
);

INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT DISTINCT role_menu.`tenant_id`, role_menu.`role_id`, button.`id`
FROM `sys_menu` button
JOIN `sys_menu` parent ON parent.`id` = button.`parent_id` AND parent.`deleted` = 0
JOIN `sys_role_menu` role_menu ON role_menu.`menu_id` = parent.`id`
WHERE button.`deleted` = 0
  AND button.`perm_key` IN (
    'basic:employee:batchDelete',
    'basic:shift:batchDelete',
    'basic:team:batchDelete',
    'basic:workshop:batchDelete',
    'basic:material:batchDelete',
    'basic:customer:batchDelete',
    'basic:supplier:batchDelete',
    'label:type:batchDelete',
    'label:field:batchDelete',
    'label:template:batchDelete',
    'label:binding:batchDelete'
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` existing
    WHERE existing.`tenant_id` = role_menu.`tenant_id`
      AND existing.`role_id` = role_menu.`role_id`
      AND existing.`menu_id` = button.`id`
  );

INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT DISTINCT role_menu.`role_id`, permission.`id`
FROM `sys_menu` button
JOIN `sys_permission` permission ON permission.`deleted` = 0 AND permission.`permission_key` = button.`perm_key`
JOIN `sys_role_menu` role_menu ON role_menu.`menu_id` = button.`id`
WHERE button.`deleted` = 0
  AND button.`perm_key` LIKE '%:batchDelete'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` existing
    WHERE existing.`role_id` = role_menu.`role_id`
      AND existing.`permission_id` = permission.`id`
  );
