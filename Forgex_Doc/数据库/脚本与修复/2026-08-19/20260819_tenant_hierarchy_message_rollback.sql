-- Roll back the 20260819 tenant hierarchy and message template upgrade.
-- Target database: forgex_admin (MySQL 8.0+).

USE `forgex_admin`;

-- Make the rollback idempotent even when the upgrade has already been rolled back.
CREATE TABLE IF NOT EXISTS `sys_tenant_parent_backup_20260819` (
  `tenant_id` bigint NOT NULL,
  `parent_tenant_id` bigint NULL,
  `backup_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `sys_tenant_created_template_backup_20260819`
  LIKE `sys_message_template`;
CREATE TABLE IF NOT EXISTS `sys_tenant_created_content_backup_20260819`
  LIKE `sys_message_template_content`;

-- Remove only rows created by this migration, then restore exact pre-existing rows.
DELETE FROM `sys_message_template_content`
WHERE `create_by` = '20260819_tenant_upgrade'
  AND `template_id` IN (
    SELECT `id` FROM `sys_message_template`
    WHERE `template_code` = 'SYS_TENANT_CREATED'
  );
DELETE FROM `sys_message_template`
WHERE `template_code` = 'SYS_TENANT_CREATED'
  AND `create_by` = '20260819_tenant_upgrade';

INSERT IGNORE INTO `sys_message_template`
SELECT * FROM `sys_tenant_created_template_backup_20260819`;
INSERT IGNORE INTO `sys_message_template_content`
SELECT * FROM `sys_tenant_created_content_backup_20260819`;

UPDATE `sys_tenant` tenant
JOIN `sys_tenant_parent_backup_20260819` backup ON backup.`tenant_id` = tenant.`id`
SET tenant.`parent_tenant_id` = backup.`parent_tenant_id`;

DROP TABLE IF EXISTS `sys_tenant_created_content_backup_20260819`;
DROP TABLE IF EXISTS `sys_tenant_created_template_backup_20260819`;
DROP TABLE IF EXISTS `sys_tenant_parent_backup_20260819`;
