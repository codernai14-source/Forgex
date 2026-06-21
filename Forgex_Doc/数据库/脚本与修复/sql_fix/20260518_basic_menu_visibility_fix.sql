-- 基础信息菜单可见性修复脚本
-- 修复范围：人员管理、车间管理、班次管理、班组管理、工作日历、包装方式
-- 适用库：forgex_admin
-- 说明：脚本可重复执行；用于修正菜单租户、补齐缺失菜单按钮、补齐接口权限并授权给当前租户管理员。

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @script_user := '20260518_basic_menu_visibility_fix';
SET @now := NOW();

-- 基础信息模块以 sys_module.code 为准，避免依赖不存在的 module_code 字段。
SET @basic_module_id := (
  SELECT `id`
  FROM `sys_module`
  WHERE `deleted` = 0 AND `code` = 'basic'
  ORDER BY `id`
  LIMIT 1
);

SET @public_tenant_id := COALESCE(
  (SELECT `tenant_id` FROM `sys_module` WHERE `id` = @basic_module_id LIMIT 1),
  1
);

SET @admin_role_id := (
  SELECT `id`
  FROM `sys_role`
  WHERE `deleted` = 0
    AND `tenant_id` = @public_tenant_id
    AND `role_key` = 'admin'
  ORDER BY `id`
  LIMIT 1
);

-- 先把已存在但落到错误租户的菜单迁回当前基础信息模块租户。
UPDATE `sys_menu`
SET
  `tenant_id` = @public_tenant_id,
  `tenant_type` = 'PUBLIC',
  `module_id` = @basic_module_id,
  `parent_id` = 0,
  `menu_level` = 1,
  `visible` = 1,
  `status` = 1,
  `update_time` = @now,
  `update_by` = @script_user
WHERE `deleted` = 0
  AND `component_key` IN ('BasicWorkshop', 'BasicEmployee', 'BasicShift', 'BasicTeam', 'BasicWorkCalendar', 'BasicPackaging');

UPDATE `sys_menu` child
JOIN `sys_menu` parent ON parent.`id` = child.`parent_id` AND parent.`deleted` = 0
SET
  child.`tenant_id` = @public_tenant_id,
  child.`tenant_type` = 'PUBLIC',
  child.`module_id` = @basic_module_id,
  child.`menu_level` = 2,
  child.`status` = 1,
  child.`update_time` = @now,
  child.`update_by` = @script_user
WHERE child.`deleted` = 0
  AND child.`type` = 'button'
  AND parent.`component_key` IN ('BasicWorkshop', 'BasicEmployee', 'BasicShift', 'BasicTeam', 'BasicWorkCalendar', 'BasicPackaging');

-- 补齐缺失的一级菜单。车间、人员、班次、班组如果已经存在，只由上面的 UPDATE 纠正租户。
INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @public_tenant_id, 'PUBLIC', @basic_module_id, 0, 'menu', seed.`path`, seed.`name`, seed.`name_i18n_json`, seed.`icon`, seed.`component_key`, seed.`perm_key`,
       seed.`order_num`, 1, 1, @now, @script_user, @now, @script_user, 0, 1, 'embedded', NULL
FROM (
  SELECT 'packaging' AS `path`, '包装方式' AS `name`, JSON_OBJECT('zh-CN','包装方式','zh-TW','包裝方式','en-US','Packaging','ja-JP','包装方式','ko-KR','포장 방식') AS `name_i18n_json`, 'InboxOutlined' AS `icon`, 'BasicPackaging' AS `component_key`, 'basic:packaging:query' AS `perm_key`, 56 AS `order_num`
  UNION ALL SELECT 'workCalendar', '工作日历', JSON_OBJECT('zh-CN','工作日历','zh-TW','工作日曆','en-US','Work Calendar','ja-JP','稼働カレンダー','ko-KR','작업 달력'), 'CalendarOutlined', 'BasicWorkCalendar', 'basic:workCalendar:query', 57
  UNION ALL SELECT 'workshop', '车间管理', JSON_OBJECT('zh-CN','车间管理','zh-TW','車間管理','en-US','Workshop','ja-JP','職場管理','ko-KR','작업장 관리'), 'HomeOutlined', 'BasicWorkshop', 'basic:workshop:query', 62
  UNION ALL SELECT 'employee', '人员管理', JSON_OBJECT('zh-CN','人员管理','zh-TW','人員管理','en-US','Employee','ja-JP','人員管理','ko-KR','인원 관리'), 'UserOutlined', 'BasicEmployee', 'basic:employee:query', 63
  UNION ALL SELECT 'shift', '班次管理', JSON_OBJECT('zh-CN','班次管理','zh-TW','班次管理','en-US','Shift','ja-JP','シフト管理','ko-KR','교대조 관리'), 'ClockCircleOutlined', 'BasicShift', 'basic:shift:query', 64
  UNION ALL SELECT 'team', '班组管理', JSON_OBJECT('zh-CN','班组管理','zh-TW','班組管理','en-US','Team','ja-JP','班組管理','ko-KR','반 관리'), 'TeamOutlined', 'BasicTeam', 'basic:team:query', 65
) seed
WHERE @basic_module_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_menu` existing
    WHERE existing.`deleted` = 0
      AND existing.`component_key` = seed.`component_key`
  );

UPDATE `sys_menu` m
JOIN (
  SELECT 'packaging' AS `path`, '包装方式' AS `name`, JSON_OBJECT('zh-CN','包装方式','zh-TW','包裝方式','en-US','Packaging','ja-JP','包装方式','ko-KR','포장 방식') AS `name_i18n_json`, 'InboxOutlined' AS `icon`, 'BasicPackaging' AS `component_key`, 'basic:packaging:query' AS `perm_key`, 56 AS `order_num`
  UNION ALL SELECT 'workCalendar', '工作日历', JSON_OBJECT('zh-CN','工作日历','zh-TW','工作日曆','en-US','Work Calendar','ja-JP','稼働カレンダー','ko-KR','작업 달력'), 'CalendarOutlined', 'BasicWorkCalendar', 'basic:workCalendar:query', 57
  UNION ALL SELECT 'workshop', '车间管理', JSON_OBJECT('zh-CN','车间管理','zh-TW','車間管理','en-US','Workshop','ja-JP','職場管理','ko-KR','작업장 관리'), 'HomeOutlined', 'BasicWorkshop', 'basic:workshop:query', 62
  UNION ALL SELECT 'employee', '人员管理', JSON_OBJECT('zh-CN','人员管理','zh-TW','人員管理','en-US','Employee','ja-JP','人員管理','ko-KR','인원 관리'), 'UserOutlined', 'BasicEmployee', 'basic:employee:query', 63
  UNION ALL SELECT 'shift', '班次管理', JSON_OBJECT('zh-CN','班次管理','zh-TW','班次管理','en-US','Shift','ja-JP','シフト管理','ko-KR','교대조 관리'), 'ClockCircleOutlined', 'BasicShift', 'basic:shift:query', 64
  UNION ALL SELECT 'team', '班组管理', JSON_OBJECT('zh-CN','班组管理','zh-TW','班組管理','en-US','Team','ja-JP','班組管理','ko-KR','반 관리'), 'TeamOutlined', 'BasicTeam', 'basic:team:query', 65
) seed ON seed.`component_key` = m.`component_key`
SET
  m.`tenant_id` = @public_tenant_id,
  m.`tenant_type` = 'PUBLIC',
  m.`module_id` = @basic_module_id,
  m.`parent_id` = 0,
  m.`type` = 'menu',
  m.`path` = seed.`path`,
  m.`name` = seed.`name`,
  m.`name_i18n_json` = seed.`name_i18n_json`,
  m.`icon` = seed.`icon`,
  m.`perm_key` = seed.`perm_key`,
  m.`order_num` = seed.`order_num`,
  m.`visible` = 1,
  m.`status` = 1,
  m.`menu_level` = 1,
  m.`menu_mode` = 'embedded',
  m.`update_time` = @now,
  m.`update_by` = @script_user
WHERE m.`deleted` = 0;

-- 补齐按钮菜单。去重条件使用 parent_id + perm_key，避免不同页面同名 query 权限互相挡住。
INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @public_tenant_id, 'PUBLIC', @basic_module_id, parent.`id`, 'button', seed.`path`, seed.`name`, seed.`name_i18n_json`, NULL, NULL,
       seed.`perm_key`, seed.`order_num`, seed.`visible`, 1, @now, @script_user, @now, @script_user, 0, 2, 'embedded', NULL
FROM (
  SELECT 'BasicPackaging' AS `parent_key`, 'query' AS `path`, '包装方式查询' AS `name`, JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회') AS `name_i18n_json`, 'basic:packaging:query' AS `perm_key`, 1 AS `order_num`, 1 AS `visible`
  UNION ALL SELECT 'BasicPackaging', 'add', '包装方式新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:packaging:add', 2, 1
  UNION ALL SELECT 'BasicPackaging', 'edit', '包装方式编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:packaging:edit', 3, 1
  UNION ALL SELECT 'BasicPackaging', 'delete', '包装方式删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:packaging:delete', 4, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'query', '工作日历查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:workCalendar:query', 1, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'add', '工作日历新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:workCalendar:add', 2, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'edit', '工作日历编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:workCalendar:edit', 3, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'delete', '工作日历删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:workCalendar:delete', 4, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'pushTenant', '工作日历推送租户', JSON_OBJECT('zh-CN','推送租户','zh-TW','推送租戶','en-US','Push Tenant','ja-JP','テナントへ配信','ko-KR','테넌트로 푸시'), 'basic:workCalendar:pushTenant', 5, 1
  UNION ALL SELECT 'BasicWorkshop', 'query', '车间查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:workshop:query', 1, 1
  UNION ALL SELECT 'BasicWorkshop', 'add', '车间新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:workshop:add', 2, 0
  UNION ALL SELECT 'BasicWorkshop', 'edit', '车间编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:workshop:edit', 3, 0
  UNION ALL SELECT 'BasicWorkshop', 'delete', '车间删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:workshop:delete', 4, 0
  UNION ALL SELECT 'BasicEmployee', 'query', '人员查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:employee:query', 1, 1
  UNION ALL SELECT 'BasicEmployee', 'add', '人员新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:employee:add', 2, 0
  UNION ALL SELECT 'BasicEmployee', 'edit', '人员编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:employee:edit', 3, 0
  UNION ALL SELECT 'BasicEmployee', 'delete', '人员删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:employee:delete', 4, 0
  UNION ALL SELECT 'BasicEmployee', 'syncUser', '同步用户', JSON_OBJECT('zh-CN','同步用户','zh-TW','同步使用者','en-US','Sync User','ja-JP','ユーザー同期','ko-KR','사용자 동기화'), 'basic:employee:syncUser', 5, 0
  UNION ALL SELECT 'BasicEmployee', 'pullThirdParty', '从第三方拉取', JSON_OBJECT('zh-CN','从第三方拉取','zh-TW','從第三方拉取','en-US','Pull Third Party','ja-JP','外部から取得','ko-KR','외부에서 가져오기'), 'basic:employee:pullThirdParty', 6, 0
  UNION ALL SELECT 'BasicEmployee', 'sync', '同步到第三方', JSON_OBJECT('zh-CN','同步到第三方','zh-TW','同步到第三方','en-US','Sync Third Party','ja-JP','外部へ同期','ko-KR','외부로 동기화'), 'basic:employee:sync', 7, 0
  UNION ALL SELECT 'BasicShift', 'query', '班次查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:shift:query', 1, 1
  UNION ALL SELECT 'BasicShift', 'add', '班次新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:shift:add', 2, 0
  UNION ALL SELECT 'BasicShift', 'edit', '班次编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:shift:edit', 3, 0
  UNION ALL SELECT 'BasicShift', 'delete', '班次删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:shift:delete', 4, 0
  UNION ALL SELECT 'BasicTeam', 'query', '班组查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:team:query', 1, 1
  UNION ALL SELECT 'BasicTeam', 'add', '班组新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:team:add', 2, 0
  UNION ALL SELECT 'BasicTeam', 'edit', '班组编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:team:edit', 3, 0
  UNION ALL SELECT 'BasicTeam', 'delete', '班组删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:team:delete', 4, 0
) seed
JOIN `sys_menu` parent
  ON parent.`deleted` = 0
 AND parent.`component_key` = seed.`parent_key`
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu` existing
  WHERE existing.`deleted` = 0
    AND existing.`parent_id` = parent.`id`
    AND existing.`perm_key` = seed.`perm_key`
);

UPDATE `sys_menu` child
JOIN `sys_menu` parent
  ON parent.`id` = child.`parent_id`
 AND parent.`deleted` = 0
JOIN (
  SELECT 'BasicPackaging' AS `parent_key`, 'query' AS `path`, '包装方式查询' AS `name`, JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회') AS `name_i18n_json`, 'basic:packaging:query' AS `perm_key`, 1 AS `order_num`, 1 AS `visible`
  UNION ALL SELECT 'BasicPackaging', 'add', '包装方式新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:packaging:add', 2, 1
  UNION ALL SELECT 'BasicPackaging', 'edit', '包装方式编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:packaging:edit', 3, 1
  UNION ALL SELECT 'BasicPackaging', 'delete', '包装方式删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:packaging:delete', 4, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'query', '工作日历查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:workCalendar:query', 1, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'add', '工作日历新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:workCalendar:add', 2, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'edit', '工作日历编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:workCalendar:edit', 3, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'delete', '工作日历删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:workCalendar:delete', 4, 1
  UNION ALL SELECT 'BasicWorkCalendar', 'pushTenant', '工作日历推送租户', JSON_OBJECT('zh-CN','推送租户','zh-TW','推送租戶','en-US','Push Tenant','ja-JP','テナントへ配信','ko-KR','테넌트로 푸시'), 'basic:workCalendar:pushTenant', 5, 1
  UNION ALL SELECT 'BasicWorkshop', 'query', '车间查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:workshop:query', 1, 1
  UNION ALL SELECT 'BasicWorkshop', 'add', '车间新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:workshop:add', 2, 0
  UNION ALL SELECT 'BasicWorkshop', 'edit', '车间编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:workshop:edit', 3, 0
  UNION ALL SELECT 'BasicWorkshop', 'delete', '车间删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:workshop:delete', 4, 0
  UNION ALL SELECT 'BasicEmployee', 'query', '人员查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:employee:query', 1, 1
  UNION ALL SELECT 'BasicEmployee', 'add', '人员新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:employee:add', 2, 0
  UNION ALL SELECT 'BasicEmployee', 'edit', '人员编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:employee:edit', 3, 0
  UNION ALL SELECT 'BasicEmployee', 'delete', '人员删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:employee:delete', 4, 0
  UNION ALL SELECT 'BasicEmployee', 'syncUser', '同步用户', JSON_OBJECT('zh-CN','同步用户','zh-TW','同步使用者','en-US','Sync User','ja-JP','ユーザー同期','ko-KR','사용자 동기화'), 'basic:employee:syncUser', 5, 0
  UNION ALL SELECT 'BasicEmployee', 'pullThirdParty', '从第三方拉取', JSON_OBJECT('zh-CN','从第三方拉取','zh-TW','從第三方拉取','en-US','Pull Third Party','ja-JP','外部から取得','ko-KR','외부에서 가져오기'), 'basic:employee:pullThirdParty', 6, 0
  UNION ALL SELECT 'BasicEmployee', 'sync', '同步到第三方', JSON_OBJECT('zh-CN','同步到第三方','zh-TW','同步到第三方','en-US','Sync Third Party','ja-JP','外部へ同期','ko-KR','외부로 동기화'), 'basic:employee:sync', 7, 0
  UNION ALL SELECT 'BasicShift', 'query', '班次查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:shift:query', 1, 1
  UNION ALL SELECT 'BasicShift', 'add', '班次新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:shift:add', 2, 0
  UNION ALL SELECT 'BasicShift', 'edit', '班次编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:shift:edit', 3, 0
  UNION ALL SELECT 'BasicShift', 'delete', '班次删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:shift:delete', 4, 0
  UNION ALL SELECT 'BasicTeam', 'query', '班组查询', JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회'), 'basic:team:query', 1, 1
  UNION ALL SELECT 'BasicTeam', 'add', '班组新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:team:add', 2, 0
  UNION ALL SELECT 'BasicTeam', 'edit', '班组编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:team:edit', 3, 0
  UNION ALL SELECT 'BasicTeam', 'delete', '班组删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:team:delete', 4, 0
) seed
  ON seed.`parent_key` = parent.`component_key`
 AND seed.`perm_key` = child.`perm_key`
SET
  child.`tenant_id` = @public_tenant_id,
  child.`tenant_type` = 'PUBLIC',
  child.`module_id` = @basic_module_id,
  child.`type` = 'button',
  child.`path` = seed.`path`,
  child.`name` = seed.`name`,
  child.`name_i18n_json` = seed.`name_i18n_json`,
  child.`order_num` = seed.`order_num`,
  child.`visible` = seed.`visible`,
  child.`status` = 1,
  child.`menu_level` = 2,
  child.`menu_mode` = 'embedded',
  child.`update_time` = @now,
  child.`update_by` = @script_user
WHERE child.`deleted` = 0;

-- 将已有按钮菜单层级、租户和模块修正为一级菜单下的按钮。
UPDATE `sys_menu` child
JOIN `sys_menu` parent ON parent.`id` = child.`parent_id` AND parent.`deleted` = 0
SET
  child.`tenant_id` = @public_tenant_id,
  child.`tenant_type` = 'PUBLIC',
  child.`module_id` = @basic_module_id,
  child.`menu_level` = 2,
  child.`status` = 1,
  child.`update_time` = @now,
  child.`update_by` = @script_user
WHERE child.`deleted` = 0
  AND child.`type` = 'button'
  AND parent.`component_key` IN ('BasicWorkshop', 'BasicEmployee', 'BasicShift', 'BasicTeam', 'BasicWorkCalendar', 'BasicPackaging');

-- 补齐接口权限。
INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT seed.`permission_name`, seed.`permission_key`, seed.`url`, 'POST', 0, @now, @now, 0
FROM (
  SELECT '包装方式查询' AS `permission_name`, 'basic:packaging:query' AS `permission_key`, '/basic/packaging/page' AS `url`
  UNION ALL SELECT '包装方式新增', 'basic:packaging:add', '/basic/packaging/create'
  UNION ALL SELECT '包装方式编辑', 'basic:packaging:edit', '/basic/packaging/update'
  UNION ALL SELECT '包装方式删除', 'basic:packaging:delete', '/basic/packaging/delete'
  UNION ALL SELECT '工作日历查询', 'basic:workCalendar:query', '/basic/work-calendar/month'
  UNION ALL SELECT '工作日历新增', 'basic:workCalendar:add', '/basic/work-calendar/event/save'
  UNION ALL SELECT '工作日历编辑', 'basic:workCalendar:edit', '/basic/work-calendar/event/save'
  UNION ALL SELECT '工作日历删除', 'basic:workCalendar:delete', '/basic/work-calendar/event/delete'
  UNION ALL SELECT '工作日历推送租户', 'basic:workCalendar:pushTenant', '/basic/work-calendar/event/push-tenant'
  UNION ALL SELECT '车间查询', 'basic:workshop:query', '/basic/workshop/page'
  UNION ALL SELECT '车间新增', 'basic:workshop:add', '/basic/workshop/create'
  UNION ALL SELECT '车间编辑', 'basic:workshop:edit', '/basic/workshop/update'
  UNION ALL SELECT '车间删除', 'basic:workshop:delete', '/basic/workshop/delete'
  UNION ALL SELECT '人员查询', 'basic:employee:query', '/basic/employee/page'
  UNION ALL SELECT '人员新增', 'basic:employee:add', '/basic/employee/create'
  UNION ALL SELECT '人员编辑', 'basic:employee:edit', '/basic/employee/update'
  UNION ALL SELECT '人员删除', 'basic:employee:delete', '/basic/employee/delete'
  UNION ALL SELECT '同步用户', 'basic:employee:syncUser', '/basic/employee/sync-user'
  UNION ALL SELECT '从第三方拉取人员', 'basic:employee:pullThirdParty', '/basic/employee/pull-from-third-party'
  UNION ALL SELECT '同步人员到第三方', 'basic:employee:sync', '/basic/employee/sync-third-party'
  UNION ALL SELECT '班次查询', 'basic:shift:query', '/basic/shift/page'
  UNION ALL SELECT '班次新增', 'basic:shift:add', '/basic/shift/create'
  UNION ALL SELECT '班次编辑', 'basic:shift:edit', '/basic/shift/update'
  UNION ALL SELECT '班次删除', 'basic:shift:delete', '/basic/shift/delete'
  UNION ALL SELECT '班组查询', 'basic:team:query', '/basic/team/page'
  UNION ALL SELECT '班组新增', 'basic:team:add', '/basic/team/create'
  UNION ALL SELECT '班组编辑', 'basic:team:edit', '/basic/team/update'
  UNION ALL SELECT '班组删除', 'basic:team:delete', '/basic/team/delete'
) seed
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_permission` existing
  WHERE existing.`deleted` = 0
    AND existing.`permission_key` = seed.`permission_key`
);

-- 已存在权限如 URL 或名称为空/旧值，顺手补齐可读信息。
UPDATE `sys_permission` p
JOIN (
  SELECT 'basic:packaging:query' AS `permission_key`, '包装方式查询' AS `permission_name`, '/basic/packaging/page' AS `url`
  UNION ALL SELECT 'basic:packaging:add', '包装方式新增', '/basic/packaging/create'
  UNION ALL SELECT 'basic:packaging:edit', '包装方式编辑', '/basic/packaging/update'
  UNION ALL SELECT 'basic:packaging:delete', '包装方式删除', '/basic/packaging/delete'
) seed ON seed.`permission_key` = p.`permission_key`
SET
  p.`permission_name` = seed.`permission_name`,
  p.`url` = seed.`url`,
  p.`method` = 'POST',
  p.`update_time` = @now
WHERE p.`deleted` = 0;

-- 授权菜单给当前租户管理员。
INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT @public_tenant_id, @admin_role_id, m.`id`
FROM `sys_menu` m
WHERE @admin_role_id IS NOT NULL
  AND m.`deleted` = 0
  AND (
    m.`component_key` IN ('BasicWorkshop', 'BasicEmployee', 'BasicShift', 'BasicTeam', 'BasicWorkCalendar', 'BasicPackaging')
    OR m.`perm_key` IN (
      'basic:packaging:query','basic:packaging:add','basic:packaging:edit','basic:packaging:delete',
      'basic:workCalendar:query','basic:workCalendar:add','basic:workCalendar:edit','basic:workCalendar:delete','basic:workCalendar:pushTenant',
      'basic:workshop:query','basic:workshop:add','basic:workshop:edit','basic:workshop:delete',
      'basic:employee:query','basic:employee:add','basic:employee:edit','basic:employee:delete','basic:employee:syncUser','basic:employee:pullThirdParty','basic:employee:sync',
      'basic:shift:query','basic:shift:add','basic:shift:edit','basic:shift:delete',
      'basic:team:query','basic:team:add','basic:team:edit','basic:team:delete'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.`tenant_id` = @public_tenant_id
      AND rm.`role_id` = @admin_role_id
      AND rm.`menu_id` = m.`id`
  );

-- 授权接口权限给当前租户管理员。
INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @admin_role_id, p.`id`
FROM `sys_permission` p
WHERE @admin_role_id IS NOT NULL
  AND p.`deleted` = 0
  AND p.`permission_key` IN (
    'basic:packaging:query','basic:packaging:add','basic:packaging:edit','basic:packaging:delete',
    'basic:workCalendar:query','basic:workCalendar:add','basic:workCalendar:edit','basic:workCalendar:delete','basic:workCalendar:pushTenant',
    'basic:workshop:query','basic:workshop:add','basic:workshop:edit','basic:workshop:delete',
    'basic:employee:query','basic:employee:add','basic:employee:edit','basic:employee:delete','basic:employee:syncUser','basic:employee:pullThirdParty','basic:employee:sync',
    'basic:shift:query','basic:shift:add','basic:shift:edit','basic:shift:delete',
    'basic:team:query','basic:team:add','basic:team:edit','basic:team:delete'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` rp
    WHERE rp.`role_id` = @admin_role_id
      AND rp.`permission_id` = p.`id`
  );

SELECT
  @basic_module_id AS `basic_module_id`,
  @public_tenant_id AS `tenant_id`,
  @admin_role_id AS `admin_role_id`,
  '基础信息菜单可见性修复完成' AS `result`;
