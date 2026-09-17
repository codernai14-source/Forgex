-- Tenant hierarchy and creation credential notification upgrade.
-- Target database: forgex_admin (MySQL 8.0+).
-- The backup tables are retained until the paired rollback script is executed.

USE `forgex_admin`;

CREATE TABLE IF NOT EXISTS `sys_tenant_parent_backup_20260819` (
  `tenant_id` bigint NOT NULL,
  `parent_tenant_id` bigint NULL,
  `backup_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='20260819 tenant parent relation rollback backup';

INSERT IGNORE INTO `sys_tenant_parent_backup_20260819` (`tenant_id`, `parent_tenant_id`)
SELECT `id`, `parent_tenant_id`
FROM `sys_tenant`
WHERE `tenant_type` <> 'MAIN_TENANT' AND `deleted` = 0;

UPDATE `sys_tenant` child_tenant
JOIN (
  SELECT `id` FROM `sys_tenant`
  WHERE `tenant_type` = 'MAIN_TENANT' AND `deleted` = 0
  ORDER BY `id` LIMIT 1
) main_tenant
LEFT JOIN `sys_tenant` current_parent ON current_parent.`id` = child_tenant.`parent_tenant_id`
SET child_tenant.`parent_tenant_id` = main_tenant.`id`
WHERE child_tenant.`tenant_type` <> 'MAIN_TENANT'
  AND child_tenant.`deleted` = 0
  AND (child_tenant.`parent_tenant_id` IS NULL
    OR current_parent.`id` IS NULL
    OR current_parent.`deleted` = 1
    OR current_parent.`tenant_type` <> 'MAIN_TENANT');

-- Back up every pre-existing instance, including soft-deleted rows, before replacing
-- the reserved template. The marker excludes rows created by a repeated run.
CREATE TABLE IF NOT EXISTS `sys_tenant_created_template_backup_20260819`
  LIKE `sys_message_template`;
CREATE TABLE IF NOT EXISTS `sys_tenant_created_content_backup_20260819`
  LIKE `sys_message_template_content`;

INSERT IGNORE INTO `sys_tenant_created_content_backup_20260819`
SELECT content.*
FROM `sys_message_template_content` content
JOIN `sys_message_template` template ON template.`id` = content.`template_id`
JOIN `sys_tenant` main_tenant ON main_tenant.`id` = template.`tenant_id`
WHERE main_tenant.`tenant_type` = 'MAIN_TENANT'
  AND main_tenant.`deleted` = 0
  AND template.`template_code` = 'SYS_TENANT_CREATED'
  AND template.`create_by` <> '20260819_tenant_upgrade';

INSERT IGNORE INTO `sys_tenant_created_template_backup_20260819`
SELECT template.*
FROM `sys_message_template` template
JOIN `sys_tenant` main_tenant ON main_tenant.`id` = template.`tenant_id`
WHERE main_tenant.`tenant_type` = 'MAIN_TENANT'
  AND main_tenant.`deleted` = 0
  AND template.`template_code` = 'SYS_TENANT_CREATED'
  AND template.`create_by` <> '20260819_tenant_upgrade';

-- Replace only the reserved code in active main tenants. Existing active or soft-
-- deleted rows are restored by rollback from the backup tables above.
DELETE content
FROM `sys_message_template_content` content
JOIN `sys_message_template` template ON template.`id` = content.`template_id`
JOIN `sys_tenant` main_tenant ON main_tenant.`id` = template.`tenant_id`
WHERE main_tenant.`tenant_type` = 'MAIN_TENANT'
  AND main_tenant.`deleted` = 0
  AND template.`template_code` = 'SYS_TENANT_CREATED';

DELETE template
FROM `sys_message_template` template
JOIN `sys_tenant` main_tenant ON main_tenant.`id` = template.`tenant_id`
WHERE main_tenant.`tenant_type` = 'MAIN_TENANT'
  AND main_tenant.`deleted` = 0
  AND template.`template_code` = 'SYS_TENANT_CREATED';

INSERT INTO `sys_message_template` (
  `tenant_id`, `template_code`, `template_name`, `template_name_i18n_json`,
  `template_version`, `message_type`, `biz_type`, `notification_type`,
  `config_level`, `tenant_type`, `category`, `status`, `remark`,
  `create_time`, `update_time`, `deleted`, `create_by`, `update_by`
)
SELECT main_tenant.`id`, 'SYS_TENANT_CREATED', '租户创建凭据通知',
  '{"zh-CN":"租户创建凭据通知","en-US":"Tenant Credentials","zh-TW":"租戶建立憑據通知","ja-JP":"テナント認証情報通知","ko-KR":"테넌트 자격 증명 알림"}',
  '1.0.0', 'NOTICE', 'TENANT_CREATED', 'success', 'TENANT', 'MAIN_TENANT', 'SYSTEM', 1,
  '新租户初始化成功后通知父租户管理员', NOW(), NOW(), 0,
  '20260819_tenant_upgrade', '20260819_tenant_upgrade'
FROM `sys_tenant` main_tenant
WHERE main_tenant.`tenant_type` = 'MAIN_TENANT' AND main_tenant.`deleted` = 0;

INSERT INTO `sys_message_template_content` (
  `tenant_id`, `template_id`, `platform`, `content_title`, `content_title_i18n_json`,
  `content_body`, `content_body_i18n_json`, `link_url`,
  `create_time`, `update_time`, `deleted`, `create_by`, `update_by`
)
SELECT template.`tenant_id`, template.`id`, 'INTERNAL',
  '【租户创建成功】${tenantName}',
  '{"zh-CN":"【租户创建成功】${tenantName}","en-US":"[Tenant Created] ${tenantName}","zh-TW":"【租戶建立成功】${tenantName}","ja-JP":"【テナント作成完了】${tenantName}","ko-KR":"【테넌트 생성 완료】${tenantName}"}',
  '租户编码：${tenantCode}\n管理员账号：${administratorAccount}\n初始密码：${initialPassword}\n请首次登录后立即修改密码。',
  '{"zh-CN":"租户编码：${tenantCode}\n管理员账号：${administratorAccount}\n初始密码：${initialPassword}\n请首次登录后立即修改密码。","en-US":"Tenant code: ${tenantCode}\nAdministrator: ${administratorAccount}\nInitial password: ${initialPassword}\nChange the password after the first login."}',
  '/workspace/sys/tenant', NOW(), NOW(), 0,
  '20260819_tenant_upgrade', '20260819_tenant_upgrade'
FROM `sys_message_template` template
JOIN `sys_tenant` main_tenant ON main_tenant.`id` = template.`tenant_id`
WHERE main_tenant.`tenant_type` = 'MAIN_TENANT'
  AND main_tenant.`deleted` = 0
  AND template.`template_code` = 'SYS_TENANT_CREATED'
  AND template.`create_by` = '20260819_tenant_upgrade';
