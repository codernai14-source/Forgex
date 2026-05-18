-- Final executed repair package.
-- First script repaired comments and compacted primary tables; second script completed remaining tables after sys_user_tenant unsigned zerofill adjustment.

-- Forgex admin ID resequence and comment repair
-- Date: 2026-05-16
-- Backup: D:\mine_product\forgex\sql_fix\forgex_admin_backup_20260516.sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `forgex_admin`;
START TRANSACTION;

-- Build old-id to new-id mapping tables.
DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_tenant`;
CREATE TEMPORARY TABLE `tmp_map_sys_tenant` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_tenant` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_name, id) AS new_id FROM `sys_tenant`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user`;
CREATE TEMPORARY TABLE `tmp_map_sys_user` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY account, id) AS new_id FROM `sys_user`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_role`;
CREATE TEMPORARY TABLE `tmp_map_sys_role` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_role` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_id, role_key, id) AS new_id FROM `sys_role`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_menu`;
CREATE TEMPORARY TABLE `tmp_map_sys_menu` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_menu` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY COALESCE(tenant_id, 0), COALESCE(module_id, 0), COALESCE(parent_id, 0), order_num, id) AS new_id FROM `sys_menu`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_dict`;
CREATE TEMPORARY TABLE `tmp_map_sys_dict` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_dict` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_id, node_path, id) AS new_id FROM `sys_dict`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_permission`;
CREATE TEMPORARY TABLE `tmp_map_sys_permission` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_permission` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY permission_key, id) AS new_id FROM `sys_permission`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_tenant`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_tenant` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_tenant` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY user_id, tenant_id, id) AS new_id FROM `sys_user_tenant`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_role`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_role` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_role` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_id, user_id, role_id, id) AS new_id FROM `sys_user_role`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_menu_favorite`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_menu_favorite` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_menu_favorite` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_id, user_id, order_num, id) AS new_id FROM `sys_user_menu_favorite`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_role_menu`;
CREATE TEMPORARY TABLE `tmp_map_sys_role_menu` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_role_menu` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_id, role_id, menu_id, id) AS new_id FROM `sys_role_menu`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_menu_common`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_menu_common` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_menu_common` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_id, user_id, visit_count DESC, id) AS new_id FROM `sys_user_menu_common`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_menu_open_count`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_menu_open_count` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_menu_open_count` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_id, user_id, last_open_at, id) AS new_id FROM `sys_user_menu_open_count`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_android_version_upload_task`;
CREATE TEMPORARY TABLE `tmp_map_sys_android_version_upload_task` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_android_version_upload_task` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY id) AS new_id FROM `sys_android_version_upload_task`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_tenant_menu_copy_rule`;
CREATE TEMPORARY TABLE `tmp_map_sys_tenant_menu_copy_rule` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_tenant_menu_copy_rule` (old_id, new_id)
SELECT id AS old_id, ROW_NUMBER() OVER (ORDER BY tenant_type, perm_prefix, id) AS new_id FROM `sys_tenant_menu_copy_rule`;

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_android_version`;
CREATE TEMPORARY TABLE `tmp_map_sys_android_version` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_android_version` (old_id, new_id) SELECT id, id FROM `sys_android_version`;

-- Repair garbled comments.
ALTER TABLE `sys_tenant_menu_copy_rule`
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  MODIFY COLUMN `tenant_type` varchar(50) NOT NULL COMMENT '租户类型编码',
  MODIFY COLUMN `perm_prefix` varchar(255) NOT NULL COMMENT '排除复制的权限前缀',
  MODIFY COLUMN `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  MODIFY COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  MODIFY COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  MODIFY COLUMN `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  MODIFY COLUMN `update_by` varchar(50) DEFAULT NULL COMMENT '修改人',
  MODIFY COLUMN `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  COMMENT = '租户菜单复制规则表';

ALTER TABLE `sys_android_version_upload_task`
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  MODIFY COLUMN `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上传任务 ID',
  MODIFY COLUMN `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名称',
  MODIFY COLUMN `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  MODIFY COLUMN `chunk_size` bigint NOT NULL DEFAULT 0 COMMENT '分片大小（字节）',
  MODIFY COLUMN `total_chunks` int NOT NULL DEFAULT 0 COMMENT '总分片数',
  MODIFY COLUMN `uploaded_chunks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '已上传分片序号',
  MODIFY COLUMN `uploaded_count` int NOT NULL DEFAULT 0 COMMENT '已上传分片数',
  MODIFY COLUMN `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UPLOADING' COMMENT '上传状态',
  MODIFY COLUMN `file_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件 SHA-256',
  MODIFY COLUMN `temp_dir` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '临时目录',
  MODIFY COLUMN `merged_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合并后文件路径',
  MODIFY COLUMN `final_file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最终文件 URL',
  MODIFY COLUMN `version_id` bigint NULL DEFAULT NULL COMMENT '版本记录 ID',
  MODIFY COLUMN `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  MODIFY COLUMN `version_code` int NULL DEFAULT NULL COMMENT '版本号',
  MODIFY COLUMN `version_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本名称',
  MODIFY COLUMN `changelog` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '更新日志',
  MODIFY COLUMN `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  MODIFY COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  MODIFY COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  MODIFY COLUMN `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  MODIFY COLUMN `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  MODIFY COLUMN `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  COMMENT = 'Android APK 分片上传任务表';

-- Update references before changing primary keys.
UPDATE `sys_android_version` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_android_version_upload_task` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_c_menu` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_codegen_config` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_codegen_datasource` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_department` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_dict` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_file_record` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_file_storage` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_homepage_component_category` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_homepage_component_config` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_homepage_component_preference` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_invite_code` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_invite_register_record` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_job` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_menu` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_message` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_message` t JOIN `tmp_map_sys_tenant` m ON t.`sender_tenant_id` = m.old_id SET t.`sender_tenant_id` = m.new_id WHERE t.`sender_tenant_id` IS NOT NULL;
UPDATE `sys_message_template` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_message_template_content` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_message_template_receiver` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_module` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_notice` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_notice_attachment` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_notice_user_record` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_permission` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_position` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_role` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_role_c_menu` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_role_dept` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_role_menu` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_role_position` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_social_login` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_tenant` t JOIN `tmp_map_sys_tenant` m ON t.`parent_tenant_id` = m.old_id SET t.`parent_tenant_id` = m.new_id WHERE t.`parent_tenant_id` IS NOT NULL;
UPDATE `sys_tenant_message_whitelist` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_tenant_message_whitelist` t JOIN `tmp_map_sys_tenant` m ON t.`sender_tenant_id` = m.old_id SET t.`sender_tenant_id` = m.new_id WHERE t.`sender_tenant_id` IS NOT NULL;
UPDATE `sys_tenant_message_whitelist` t JOIN `tmp_map_sys_tenant` m ON t.`receiver_tenant_id` = m.old_id SET t.`receiver_tenant_id` = m.new_id WHERE t.`receiver_tenant_id` IS NOT NULL;
UPDATE `sys_user` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_user_c_menu_favorite` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_user_menu_common` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_user_menu_favorite` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_user_menu_open_count` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_user_profile` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_user_role` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_user_tenant` t JOIN `tmp_map_sys_tenant` m ON t.`tenant_id` = m.old_id SET t.`tenant_id` = m.new_id WHERE t.`tenant_id` IS NOT NULL;
UPDATE `sys_homepage_component_preference` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_invite_register_record` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_message` t JOIN `tmp_map_sys_user` m ON t.`sender_user_id` = m.old_id SET t.`sender_user_id` = m.new_id WHERE t.`sender_user_id` IS NOT NULL;
UPDATE `sys_message` t JOIN `tmp_map_sys_user` m ON t.`receiver_user_id` = m.old_id SET t.`receiver_user_id` = m.new_id WHERE t.`receiver_user_id` IS NOT NULL;
UPDATE `sys_notice_user_record` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_social_login` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_user_c_menu_favorite` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_user_menu_common` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_user_menu_favorite` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_user_menu_open_count` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_user_profile` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_user_role` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_user_tenant` t JOIN `tmp_map_sys_user` m ON t.`user_id` = m.old_id SET t.`user_id` = m.new_id WHERE t.`user_id` IS NOT NULL;
UPDATE `sys_invite_code` t JOIN `tmp_map_sys_role` m ON t.`role_id` = m.old_id SET t.`role_id` = m.new_id WHERE t.`role_id` IS NOT NULL;
UPDATE `sys_invite_register_record` t JOIN `tmp_map_sys_role` m ON t.`role_id` = m.old_id SET t.`role_id` = m.new_id WHERE t.`role_id` IS NOT NULL;
UPDATE `sys_role_c_menu` t JOIN `tmp_map_sys_role` m ON t.`role_id` = m.old_id SET t.`role_id` = m.new_id WHERE t.`role_id` IS NOT NULL;
UPDATE `sys_role_dept` t JOIN `tmp_map_sys_role` m ON t.`role_id` = m.old_id SET t.`role_id` = m.new_id WHERE t.`role_id` IS NOT NULL;
UPDATE `sys_role_menu` t JOIN `tmp_map_sys_role` m ON t.`role_id` = m.old_id SET t.`role_id` = m.new_id WHERE t.`role_id` IS NOT NULL;
UPDATE `sys_role_permission` t JOIN `tmp_map_sys_role` m ON t.`role_id` = m.old_id SET t.`role_id` = m.new_id WHERE t.`role_id` IS NOT NULL;
UPDATE `sys_role_position` t JOIN `tmp_map_sys_role` m ON t.`role_id` = m.old_id SET t.`role_id` = m.new_id WHERE t.`role_id` IS NOT NULL;
UPDATE `sys_user_role` t JOIN `tmp_map_sys_role` m ON t.`role_id` = m.old_id SET t.`role_id` = m.new_id WHERE t.`role_id` IS NOT NULL;
UPDATE `sys_menu` t JOIN `tmp_map_sys_menu` m ON t.`parent_id` = m.old_id SET t.`parent_id` = m.new_id WHERE t.`parent_id` IS NOT NULL;
UPDATE `sys_role_menu` t JOIN `tmp_map_sys_menu` m ON t.`menu_id` = m.old_id SET t.`menu_id` = m.new_id WHERE t.`menu_id` IS NOT NULL;
UPDATE `sys_dict` t JOIN `tmp_map_sys_dict` m ON t.`parent_id` = m.old_id SET t.`parent_id` = m.new_id WHERE t.`parent_id` IS NOT NULL;
UPDATE `sys_role_permission` t JOIN `tmp_map_sys_permission` m ON t.`permission_id` = m.old_id SET t.`permission_id` = m.new_id WHERE t.`permission_id` IS NOT NULL;
UPDATE `sys_android_version_upload_task` t JOIN `tmp_map_sys_android_version` m ON t.`version_id` = m.old_id SET t.`version_id` = m.new_id WHERE t.`version_id` IS NOT NULL;

-- Reassign primary keys via negative temporary IDs to avoid collision.
UPDATE `sys_tenant` t JOIN `tmp_map_sys_tenant` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_tenant` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_user` t JOIN `tmp_map_sys_user` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_user` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_role` t JOIN `tmp_map_sys_role` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_role` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_menu` t JOIN `tmp_map_sys_menu` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_menu` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_dict` t JOIN `tmp_map_sys_dict` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_dict` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_permission` t JOIN `tmp_map_sys_permission` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_permission` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_user_tenant` t JOIN `tmp_map_sys_user_tenant` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_user_tenant` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_user_role` t JOIN `tmp_map_sys_user_role` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_user_role` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_user_menu_favorite` t JOIN `tmp_map_sys_user_menu_favorite` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_user_menu_favorite` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_role_menu` t JOIN `tmp_map_sys_role_menu` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_role_menu` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_user_menu_common` t JOIN `tmp_map_sys_user_menu_common` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_user_menu_common` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_user_menu_open_count` t JOIN `tmp_map_sys_user_menu_open_count` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_user_menu_open_count` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_android_version_upload_task` t JOIN `tmp_map_sys_android_version_upload_task` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_android_version_upload_task` SET `id` = -`id` WHERE `id` < 0;
UPDATE `sys_tenant_menu_copy_rule` t JOIN `tmp_map_sys_tenant_menu_copy_rule` m ON t.`id` = m.old_id SET t.`id` = -m.new_id;
UPDATE `sys_tenant_menu_copy_rule` SET `id` = -`id` WHERE `id` < 0;

-- Normalize auto_increment counters to MAX(id)+1 after resequence.
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_tenant`);
SET @sql := CONCAT('ALTER TABLE `sys_tenant` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user`);
SET @sql := CONCAT('ALTER TABLE `sys_user` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_role`);
SET @sql := CONCAT('ALTER TABLE `sys_role` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_menu` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_dict`);
SET @sql := CONCAT('ALTER TABLE `sys_dict` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_permission`);
SET @sql := CONCAT('ALTER TABLE `sys_permission` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_tenant`);
SET @sql := CONCAT('ALTER TABLE `sys_user_tenant` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_role`);
SET @sql := CONCAT('ALTER TABLE `sys_user_role` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_menu_favorite`);
SET @sql := CONCAT('ALTER TABLE `sys_user_menu_favorite` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_role_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_role_menu` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_menu_common`);
SET @sql := CONCAT('ALTER TABLE `sys_user_menu_common` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_menu_open_count`);
SET @sql := CONCAT('ALTER TABLE `sys_user_menu_open_count` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_android_version_upload_task`);
SET @sql := CONCAT('ALTER TABLE `sys_android_version_upload_task` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_tenant_menu_copy_rule`);
SET @sql := CONCAT('ALTER TABLE `sys_tenant_menu_copy_rule` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

SELECT TABLE_NAME, TABLE_ROWS, AUTO_INCREMENT, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA='forgex_admin' AND TABLE_NAME IN ('sys_tenant','sys_user','sys_role','sys_menu','sys_dict','sys_permission','sys_user_tenant','sys_user_role','sys_user_menu_favorite','sys_role_menu','sys_user_menu_common','sys_user_menu_open_count','sys_android_version_upload_task','sys_tenant_menu_copy_rule') ORDER BY TABLE_NAME;


-- ==================== PATCH EXECUTED AFTER UNSIGNED ZEROFILL FIX ====================

-- Patch remaining tables after unsigned zerofill issue
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `forgex_admin`;
START TRANSACTION;

ALTER TABLE `sys_user_tenant` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id??';

DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_tenant`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_tenant` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_tenant` (old_id, new_id) SELECT id, ROW_NUMBER() OVER (ORDER BY user_id, tenant_id, id) FROM `sys_user_tenant`;
DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_role`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_role` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_role` (old_id, new_id) SELECT id, ROW_NUMBER() OVER (ORDER BY tenant_id, user_id, role_id, id) FROM `sys_user_role`;
DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_menu_favorite`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_menu_favorite` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_menu_favorite` (old_id, new_id) SELECT id, ROW_NUMBER() OVER (ORDER BY tenant_id, user_id, order_num, id) FROM `sys_user_menu_favorite`;
DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_role_menu`;
CREATE TEMPORARY TABLE `tmp_map_sys_role_menu` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_role_menu` (old_id, new_id) SELECT id, ROW_NUMBER() OVER (ORDER BY tenant_id, role_id, menu_id, id) FROM `sys_role_menu`;
DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_menu_common`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_menu_common` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_menu_common` (old_id, new_id) SELECT id, ROW_NUMBER() OVER (ORDER BY tenant_id, user_id, visit_count DESC, id) FROM `sys_user_menu_common`;
DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_user_menu_open_count`;
CREATE TEMPORARY TABLE `tmp_map_sys_user_menu_open_count` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_user_menu_open_count` (old_id, new_id) SELECT id, ROW_NUMBER() OVER (ORDER BY tenant_id, user_id, last_open_at, id) FROM `sys_user_menu_open_count`;
DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_android_version_upload_task`;
CREATE TEMPORARY TABLE `tmp_map_sys_android_version_upload_task` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_android_version_upload_task` (old_id, new_id) SELECT id, ROW_NUMBER() OVER (ORDER BY id) FROM `sys_android_version_upload_task`;
DROP TEMPORARY TABLE IF EXISTS `tmp_map_sys_tenant_menu_copy_rule`;
CREATE TEMPORARY TABLE `tmp_map_sys_tenant_menu_copy_rule` (old_id BIGINT NOT NULL PRIMARY KEY, new_id BIGINT NOT NULL UNIQUE) ENGINE=MEMORY;
INSERT INTO `tmp_map_sys_tenant_menu_copy_rule` (old_id, new_id) SELECT id, ROW_NUMBER() OVER (ORDER BY tenant_type, perm_prefix, id) FROM `sys_tenant_menu_copy_rule`;

-- Reassign primary keys using high temporary IDs to support unsigned columns.
UPDATE `sys_user_tenant` t JOIN `tmp_map_sys_user_tenant` m ON t.`id` = m.old_id SET t.`id` = 9000000000000000000 + m.new_id;
UPDATE `sys_user_tenant` t JOIN `tmp_map_sys_user_tenant` m ON t.`id` = 9000000000000000000 + m.new_id SET t.`id` = m.new_id;
UPDATE `sys_user_role` t JOIN `tmp_map_sys_user_role` m ON t.`id` = m.old_id SET t.`id` = 9000000000000000000 + m.new_id;
UPDATE `sys_user_role` t JOIN `tmp_map_sys_user_role` m ON t.`id` = 9000000000000000000 + m.new_id SET t.`id` = m.new_id;
UPDATE `sys_user_menu_favorite` t JOIN `tmp_map_sys_user_menu_favorite` m ON t.`id` = m.old_id SET t.`id` = 9000000000000000000 + m.new_id;
UPDATE `sys_user_menu_favorite` t JOIN `tmp_map_sys_user_menu_favorite` m ON t.`id` = 9000000000000000000 + m.new_id SET t.`id` = m.new_id;
UPDATE `sys_role_menu` t JOIN `tmp_map_sys_role_menu` m ON t.`id` = m.old_id SET t.`id` = 9000000000000000000 + m.new_id;
UPDATE `sys_role_menu` t JOIN `tmp_map_sys_role_menu` m ON t.`id` = 9000000000000000000 + m.new_id SET t.`id` = m.new_id;
UPDATE `sys_user_menu_common` t JOIN `tmp_map_sys_user_menu_common` m ON t.`id` = m.old_id SET t.`id` = 9000000000000000000 + m.new_id;
UPDATE `sys_user_menu_common` t JOIN `tmp_map_sys_user_menu_common` m ON t.`id` = 9000000000000000000 + m.new_id SET t.`id` = m.new_id;
UPDATE `sys_user_menu_open_count` t JOIN `tmp_map_sys_user_menu_open_count` m ON t.`id` = m.old_id SET t.`id` = 9000000000000000000 + m.new_id;
UPDATE `sys_user_menu_open_count` t JOIN `tmp_map_sys_user_menu_open_count` m ON t.`id` = 9000000000000000000 + m.new_id SET t.`id` = m.new_id;
UPDATE `sys_android_version_upload_task` t JOIN `tmp_map_sys_android_version_upload_task` m ON t.`id` = m.old_id SET t.`id` = 9000000000000000000 + m.new_id;
UPDATE `sys_android_version_upload_task` t JOIN `tmp_map_sys_android_version_upload_task` m ON t.`id` = 9000000000000000000 + m.new_id SET t.`id` = m.new_id;
UPDATE `sys_tenant_menu_copy_rule` t JOIN `tmp_map_sys_tenant_menu_copy_rule` m ON t.`id` = m.old_id SET t.`id` = 9000000000000000000 + m.new_id;
UPDATE `sys_tenant_menu_copy_rule` t JOIN `tmp_map_sys_tenant_menu_copy_rule` m ON t.`id` = 9000000000000000000 + m.new_id SET t.`id` = m.new_id;

-- Normalize auto_increment counters.
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_tenant`);
SET @sql := CONCAT('ALTER TABLE `sys_user_tenant` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_role`);
SET @sql := CONCAT('ALTER TABLE `sys_user_role` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_menu_favorite`);
SET @sql := CONCAT('ALTER TABLE `sys_user_menu_favorite` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_role_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_role_menu` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_menu_common`);
SET @sql := CONCAT('ALTER TABLE `sys_user_menu_common` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user_menu_open_count`);
SET @sql := CONCAT('ALTER TABLE `sys_user_menu_open_count` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_android_version_upload_task`);
SET @sql := CONCAT('ALTER TABLE `sys_android_version_upload_task` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_tenant_menu_copy_rule`);
SET @sql := CONCAT('ALTER TABLE `sys_tenant_menu_copy_rule` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_tenant`);
SET @sql := CONCAT('ALTER TABLE `sys_tenant` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_user`);
SET @sql := CONCAT('ALTER TABLE `sys_user` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_role`);
SET @sql := CONCAT('ALTER TABLE `sys_role` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_menu`);
SET @sql := CONCAT('ALTER TABLE `sys_menu` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_dict`);
SET @sql := CONCAT('ALTER TABLE `sys_dict` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_ai := (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_permission`);
SET @sql := CONCAT('ALTER TABLE `sys_permission` AUTO_INCREMENT = ', @next_ai);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
SELECT TABLE_NAME, TABLE_ROWS, AUTO_INCREMENT, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA='forgex_admin' AND TABLE_NAME IN ('sys_tenant','sys_user','sys_role','sys_menu','sys_dict','sys_permission','sys_user_tenant','sys_user_role','sys_user_menu_favorite','sys_role_menu','sys_user_menu_common','sys_user_menu_open_count','sys_android_version_upload_task','sys_tenant_menu_copy_rule') ORDER BY TABLE_NAME;
