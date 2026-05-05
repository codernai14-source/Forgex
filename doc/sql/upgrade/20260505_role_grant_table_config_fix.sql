SET NAMES utf8mb4;

-- ============================================================
-- 2026-05-05 role grant dynamic table configuration fix
-- Target schema: forgex_common
-- Safe to run repeatedly.
-- ============================================================

USE `forgex_common`;

SET @OPERATOR := '20260505_role_grant_table_config_fix';

DROP TEMPORARY TABLE IF EXISTS `tmp_role_grant_tenants`;
CREATE TEMPORARY TABLE `tmp_role_grant_tenants` (
  `tenant_id` bigint NOT NULL PRIMARY KEY
) ENGINE=Memory;

INSERT IGNORE INTO `tmp_role_grant_tenants` (`tenant_id`) VALUES (0);

INSERT IGNORE INTO `tmp_role_grant_tenants` (`tenant_id`)
SELECT `id`
FROM `forgex_admin`.`sys_tenant`
WHERE `status` = 1
  AND `deleted` = 0;

INSERT INTO `fx_table_config` (
  `tenant_id`,
  `table_code`,
  `table_name_i18n_json`,
  `table_type`,
  `row_key`,
  `default_page_size`,
  `enabled`,
  `version`,
  `create_by`,
  `create_time`,
  `update_by`,
  `update_time`,
  `deleted`
)
SELECT
  t.`tenant_id`,
  cfg.`table_code`,
  cfg.`table_name_i18n_json`,
  cfg.`table_type`,
  'id',
  20,
  1,
  1,
  @OPERATOR,
  NOW(),
  @OPERATOR,
  NOW(),
  0
FROM `tmp_role_grant_tenants` t
JOIN (
  SELECT
    'RoleMenuGrantTable' AS `table_code`,
    '{"zh-CN":"角色菜单授权","en-US":"Role Menu Grant","zh-TW":"角色選單授權","ja-JP":"ロールメニュー権限付与","ko-KR":"역할 메뉴 권한 부여"}' AS `table_name_i18n_json`,
    'TREE' AS `table_type`
  UNION ALL
  SELECT
    'RoleUserGrantTable',
    '{"zh-CN":"角色人员授权","en-US":"Role User Grant","zh-TW":"角色人員授權","ja-JP":"ロールユーザー権限付与","ko-KR":"역할 사용자 권한 부여"}',
    'NORMAL'
) cfg
WHERE NOT EXISTS (
  SELECT 1
  FROM `fx_table_config` existing_cfg
  WHERE existing_cfg.`tenant_id` = t.`tenant_id`
    AND existing_cfg.`table_code` = cfg.`table_code`
);

UPDATE `fx_table_config` cfg
JOIN `tmp_role_grant_tenants` t
  ON t.`tenant_id` = cfg.`tenant_id`
JOIN (
  SELECT
    'RoleMenuGrantTable' AS `table_code`,
    '{"zh-CN":"角色菜单授权","en-US":"Role Menu Grant","zh-TW":"角色選單授權","ja-JP":"ロールメニュー権限付与","ko-KR":"역할 메뉴 권한 부여"}' AS `table_name_i18n_json`,
    'TREE' AS `table_type`
  UNION ALL
  SELECT
    'RoleUserGrantTable',
    '{"zh-CN":"角色人员授权","en-US":"Role User Grant","zh-TW":"角色人員授權","ja-JP":"ロールユーザー権限付与","ko-KR":"역할 사용자 권한 부여"}',
    'NORMAL'
) fixed_cfg
  ON fixed_cfg.`table_code` = cfg.`table_code`
SET cfg.`table_name_i18n_json` = fixed_cfg.`table_name_i18n_json`,
    cfg.`table_type` = fixed_cfg.`table_type`,
    cfg.`row_key` = 'id',
    cfg.`default_page_size` = 20,
    cfg.`enabled` = 1,
    cfg.`deleted` = 0,
    cfg.`update_by` = @OPERATOR,
    cfg.`update_time` = NOW()
WHERE cfg.`table_code` IN ('RoleMenuGrantTable', 'RoleUserGrantTable');

DROP TEMPORARY TABLE IF EXISTS `tmp_role_grant_columns`;
CREATE TEMPORARY TABLE `tmp_role_grant_columns` (
  `table_code` varchar(128) NOT NULL,
  `field` varchar(128) NOT NULL,
  `title_i18n_json` varchar(1000) NOT NULL,
  `align` varchar(16) NULL,
  `width` int NULL,
  `fixed` varchar(16) NULL,
  `ellipsis` tinyint NOT NULL DEFAULT 0,
  `sortable` tinyint NOT NULL DEFAULT 0,
  `sorter_field` varchar(128) NULL,
  `queryable` tinyint NOT NULL DEFAULT 0,
  `query_type` varchar(32) NULL,
  `query_operator` varchar(32) NULL,
  `dict_code` varchar(100) NULL,
  `render_type` varchar(32) NULL,
  `perm_key` varchar(100) NULL,
  `order_num` int NOT NULL,
  PRIMARY KEY (`table_code`, `field`)
) ENGINE=Memory;

INSERT INTO `tmp_role_grant_columns` (
  `table_code`,
  `field`,
  `title_i18n_json`,
  `align`,
  `width`,
  `fixed`,
  `ellipsis`,
  `sortable`,
  `sorter_field`,
  `queryable`,
  `query_type`,
  `query_operator`,
  `dict_code`,
  `render_type`,
  `perm_key`,
  `order_num`
) VALUES
  ('RoleMenuGrantTable', 'name', '{"zh-CN":"菜单名称","en-US":"Menu Name","zh-TW":"選單名稱","ja-JP":"メニュー名","ko-KR":"메뉴명"}', 'left', 220, NULL, 1, 0, NULL, 1, 'input', 'like', NULL, 'text', NULL, 1),
  ('RoleMenuGrantTable', 'type', '{"zh-CN":"菜单类型","en-US":"Menu Type","zh-TW":"選單類型","ja-JP":"メニュータイプ","ko-KR":"메뉴 유형"}', 'left', 110, NULL, 0, 0, NULL, 1, 'select', 'eq', 'menu_type', 'dictTag', NULL, 2),
  ('RoleMenuGrantTable', 'path', '{"zh-CN":"路径","en-US":"Path","zh-TW":"路徑","ja-JP":"パス","ko-KR":"경로"}', 'left', 180, NULL, 1, 0, NULL, 1, 'input', 'like', NULL, 'text', NULL, 3),
  ('RoleMenuGrantTable', 'permKey', '{"zh-CN":"权限标识","en-US":"Permission Key","zh-TW":"權限標識","ja-JP":"権限キー","ko-KR":"권한 키"}', 'left', 220, NULL, 1, 0, NULL, 1, 'input', 'like', NULL, 'text', NULL, 4),
  ('RoleMenuGrantTable', 'status', '{"zh-CN":"状态","en-US":"Status","zh-TW":"狀態","ja-JP":"状態","ko-KR":"상태"}', 'left', 100, NULL, 0, 0, NULL, 1, 'select', 'eq', 'status', 'dictTag', NULL, 5),
  ('RoleUserGrantTable', 'grantType', '{"zh-CN":"授权类型","en-US":"Grant Type","zh-TW":"授權類型","ja-JP":"権限付与タイプ","ko-KR":"권한 부여 유형"}', 'left', 120, NULL, 0, 0, NULL, 1, 'select', 'eq', 'role_grant_type', 'text', NULL, 1),
  ('RoleUserGrantTable', 'grantObject', '{"zh-CN":"授权对象","en-US":"Grant Object","zh-TW":"授權對象","ja-JP":"権限付与対象","ko-KR":"권한 부여 대상"}', 'left', 180, NULL, 1, 0, NULL, 1, 'input', 'like', NULL, 'text', NULL, 2),
  ('RoleUserGrantTable', 'grantObjectCode', '{"zh-CN":"对象编码","en-US":"Object Code","zh-TW":"對象編碼","ja-JP":"対象コード","ko-KR":"대상 코드"}', 'left', 160, NULL, 1, 0, NULL, 1, 'input', 'like', NULL, 'text', NULL, 3),
  ('RoleUserGrantTable', 'createTime', '{"zh-CN":"授权时间","en-US":"Grant Time","zh-TW":"授權時間","ja-JP":"権限付与日時","ko-KR":"권한 부여 시간"}', 'left', 180, NULL, 0, 1, 'create_time', 0, NULL, NULL, NULL, 'date', NULL, 4),
  ('RoleUserGrantTable', 'createBy', '{"zh-CN":"授权人","en-US":"Grant By","zh-TW":"授權人","ja-JP":"権限付与者","ko-KR":"권한 부여자"}', 'left', 120, NULL, 1, 0, NULL, 0, NULL, NULL, NULL, 'text', NULL, 5),
  ('RoleUserGrantTable', 'action', '{"zh-CN":"操作","en-US":"Action","zh-TW":"操作","ja-JP":"操作","ko-KR":"작업"}', 'left', 100, 'right', 0, 0, NULL, 0, NULL, NULL, NULL, 'slot', NULL, 6);

INSERT INTO `fx_table_column_config` (
  `tenant_id`,
  `table_code`,
  `field`,
  `title_i18n_json`,
  `align`,
  `width`,
  `fixed`,
  `ellipsis`,
  `sortable`,
  `sorter_field`,
  `queryable`,
  `query_type`,
  `query_operator`,
  `dict_code`,
  `render_type`,
  `perm_key`,
  `order_num`,
  `enabled`,
  `create_by`,
  `create_time`,
  `update_by`,
  `update_time`,
  `deleted`
)
SELECT
  t.`tenant_id`,
  c.`table_code`,
  c.`field`,
  c.`title_i18n_json`,
  c.`align`,
  c.`width`,
  c.`fixed`,
  c.`ellipsis`,
  c.`sortable`,
  c.`sorter_field`,
  c.`queryable`,
  c.`query_type`,
  c.`query_operator`,
  c.`dict_code`,
  c.`render_type`,
  c.`perm_key`,
  c.`order_num`,
  1,
  @OPERATOR,
  NOW(),
  @OPERATOR,
  NOW(),
  0
FROM `tmp_role_grant_tenants` t
JOIN `tmp_role_grant_columns` c
WHERE NOT EXISTS (
  SELECT 1
  FROM `fx_table_column_config` existing_col
  WHERE existing_col.`tenant_id` = t.`tenant_id`
    AND existing_col.`table_code` = c.`table_code`
    AND existing_col.`field` = c.`field`
    AND existing_col.`deleted` = 0
);

UPDATE `fx_table_column_config` col
JOIN `tmp_role_grant_tenants` t
  ON t.`tenant_id` = col.`tenant_id`
JOIN `tmp_role_grant_columns` fixed_col
  ON fixed_col.`table_code` = col.`table_code`
 AND fixed_col.`field` = col.`field`
SET col.`title_i18n_json` = fixed_col.`title_i18n_json`,
    col.`align` = fixed_col.`align`,
    col.`width` = fixed_col.`width`,
    col.`fixed` = fixed_col.`fixed`,
    col.`ellipsis` = fixed_col.`ellipsis`,
    col.`sortable` = fixed_col.`sortable`,
    col.`sorter_field` = fixed_col.`sorter_field`,
    col.`queryable` = fixed_col.`queryable`,
    col.`query_type` = fixed_col.`query_type`,
    col.`query_operator` = fixed_col.`query_operator`,
    col.`dict_code` = fixed_col.`dict_code`,
    col.`render_type` = fixed_col.`render_type`,
    col.`perm_key` = fixed_col.`perm_key`,
    col.`order_num` = fixed_col.`order_num`,
    col.`enabled` = 1,
    col.`deleted` = 0,
    col.`update_by` = @OPERATOR,
    col.`update_time` = NOW()
WHERE col.`table_code` IN ('RoleMenuGrantTable', 'RoleUserGrantTable');

UPDATE `fx_user_table_config`
SET `deleted` = 1,
    `update_by` = @OPERATOR,
    `update_time` = NOW()
WHERE `table_code` IN ('RoleMenuGrantTable', 'RoleUserGrantTable')
  AND `deleted` = 0;

DROP TEMPORARY TABLE IF EXISTS `tmp_role_grant_columns`;
DROP TEMPORARY TABLE IF EXISTS `tmp_role_grant_tenants`;
