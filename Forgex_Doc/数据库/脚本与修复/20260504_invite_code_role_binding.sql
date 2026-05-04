SET NAMES utf8mb4;

-- ============================================================
-- 2026-05-04 invite code role binding upgrade
-- Target schemas: forgex_admin, forgex_common
-- Safe to run repeatedly.
-- ============================================================

USE `forgex_admin`;

SET @OPERATOR := '20260504_invite_role';

DELIMITER $$

DROP PROCEDURE IF EXISTS `forgex_admin`.`fx_add_column_if_missing` $$
CREATE PROCEDURE `forgex_admin`.`fx_add_column_if_missing`(
  IN p_schema varchar(64),
  IN p_table varchar(64),
  IN p_column varchar(64),
  IN p_ddl text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = p_schema
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column
  ) THEN
    SET @fx_sql := p_ddl;
    PREPARE fx_stmt FROM @fx_sql;
    EXECUTE fx_stmt;
    DEALLOCATE PREPARE fx_stmt;
  END IF;
END $$

DROP PROCEDURE IF EXISTS `forgex_admin`.`fx_add_index_if_missing` $$
CREATE PROCEDURE `forgex_admin`.`fx_add_index_if_missing`(
  IN p_schema varchar(64),
  IN p_table varchar(64),
  IN p_index varchar(64),
  IN p_ddl text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = p_schema
      AND TABLE_NAME = p_table
      AND INDEX_NAME = p_index
  ) THEN
    SET @fx_sql := p_ddl;
    PREPARE fx_stmt FROM @fx_sql;
    EXECUTE fx_stmt;
    DEALLOCATE PREPARE fx_stmt;
  END IF;
END $$

DELIMITER ;

CALL `forgex_admin`.`fx_add_column_if_missing`(
  'forgex_admin',
  'sys_invite_code',
  'role_id',
  'ALTER TABLE `forgex_admin`.`sys_invite_code` ADD COLUMN `role_id` bigint NULL DEFAULT NULL COMMENT ''注册后绑定角色ID'' AFTER `position_id`'
);

CALL `forgex_admin`.`fx_add_column_if_missing`(
  'forgex_admin',
  'sys_invite_register_record',
  'role_id',
  'ALTER TABLE `forgex_admin`.`sys_invite_register_record` ADD COLUMN `role_id` bigint NULL DEFAULT NULL COMMENT ''注册绑定角色ID'' AFTER `position_id`'
);

CALL `forgex_admin`.`fx_add_index_if_missing`(
  'forgex_admin',
  'sys_invite_code',
  'idx_role_id',
  'ALTER TABLE `forgex_admin`.`sys_invite_code` ADD INDEX `idx_role_id` (`role_id`)'
);

CALL `forgex_admin`.`fx_add_index_if_missing`(
  'forgex_admin',
  'sys_invite_register_record',
  'idx_role_id',
  'ALTER TABLE `forgex_admin`.`sys_invite_register_record` ADD INDEX `idx_role_id` (`role_id`)'
);

UPDATE `forgex_admin`.`sys_invite_code` ic
JOIN `forgex_admin`.`sys_role` r
  ON r.tenant_id = ic.tenant_id
 AND r.role_key = 'user'
 AND r.status = 1
 AND r.deleted = 0
SET ic.role_id = r.id,
    ic.update_by = @OPERATOR,
    ic.update_time = NOW()
WHERE ic.role_id IS NULL
  AND ic.deleted = 0;

USE `forgex_common`;

SET @INVITE_ROLE_NAME_EXISTS := (
  SELECT COUNT(1)
  FROM `forgex_common`.`fx_table_column_config`
  WHERE tenant_id = 0
    AND table_code = 'InviteCodeTable'
    AND field = 'roleName'
    AND deleted = 0
);

UPDATE `forgex_common`.`fx_table_column_config`
SET order_num = order_num + 10,
    update_by = @OPERATOR,
    update_time = NOW()
WHERE tenant_id = 0
  AND table_code = 'InviteCodeTable'
  AND deleted = 0
  AND order_num >= 40
  AND @INVITE_ROLE_NAME_EXISTS = 0;

INSERT INTO `forgex_common`.`fx_table_column_config` (
  tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis,
  sortable, sorter_field, queryable, query_type, query_operator, dict_code, render_type, perm_key,
  order_num, enabled, create_by, create_time, update_by, update_time, deleted
)
SELECT 0, 'InviteCodeTable', 'roleName',
       '{"en-US":"Role","ja-JP":"ロール","ko-KR":"역할","zh-CN":"绑定角色","zh-TW":"綁定角色"}',
       'left', 140, NULL, 0,
       0, NULL, 0, NULL, NULL, NULL, 'text', NULL,
       40, 1, @OPERATOR, NOW(), @OPERATOR, NOW(), 0
WHERE NOT EXISTS (
  SELECT 1
  FROM `forgex_common`.`fx_table_column_config` c
  WHERE c.tenant_id = 0
    AND c.table_code = 'InviteCodeTable'
    AND c.field = 'roleName'
    AND c.deleted = 0
);

INSERT INTO `forgex_common`.`fx_table_column_config` (
  tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis,
  sortable, sorter_field, queryable, query_type, query_operator, dict_code, render_type, perm_key,
  order_num, enabled, create_by, create_time, update_by, update_time, deleted
)
SELECT 0, 'InviteCodeTable', 'roleId',
       '{"en-US":"Role","ja-JP":"ロール","ko-KR":"역할","zh-CN":"绑定角色","zh-TW":"綁定角色"}',
       'left', 140, NULL, 0,
       0, NULL, 1, 'select', 'eq', 'role', 'text', NULL,
       41, 0, @OPERATOR, NOW(), @OPERATOR, NOW(), 0
WHERE NOT EXISTS (
  SELECT 1
  FROM `forgex_common`.`fx_table_column_config` c
  WHERE c.tenant_id = 0
    AND c.table_code = 'InviteCodeTable'
    AND c.field = 'roleId'
    AND c.deleted = 0
);

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1,
    query_type = 'select',
    query_operator = 'eq',
    dict_code = 'role',
    enabled = 0,
    update_by = @OPERATOR,
    update_time = NOW()
WHERE tenant_id = 0
  AND table_code = 'InviteCodeTable'
  AND field = 'roleId'
  AND deleted = 0;

UPDATE `forgex_common`.`fx_table_column_config`
SET enabled = 1,
    order_num = 40,
    render_type = 'text',
    update_by = @OPERATOR,
    update_time = NOW()
WHERE tenant_id = 0
  AND table_code = 'InviteCodeTable'
  AND field = 'roleName'
  AND deleted = 0;

USE `forgex_admin`;

DROP PROCEDURE IF EXISTS `forgex_admin`.`fx_add_column_if_missing`;
DROP PROCEDURE IF EXISTS `forgex_admin`.`fx_add_index_if_missing`;
