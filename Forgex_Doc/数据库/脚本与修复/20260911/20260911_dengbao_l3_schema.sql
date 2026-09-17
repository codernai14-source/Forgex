-- 等保三级软件层整改：已有环境 schema 补齐
-- 适用库：forgex_admin、forgex_history；若存在 basic_employee / basic_supplier 则一并补密级列
-- 说明：幂等。登录查询会读取 sys_user.mfa_enabled / must_change_pwd / pwd_update_time，缺列会报 Unknown column。
-- 回滚：同目录 20260911_dengbao_l3_schema_rollback.sql

-- ---------------------------------------------------------------------------
-- 1. 管理库：口令生命周期、MFA、密级
-- ---------------------------------------------------------------------------
USE `forgex_admin`;

CREATE TABLE IF NOT EXISTS `sys_user_password_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '历史口令哈希',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '写入时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_pwd_history_user` (`tenant_id`, `user_id`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户口令历史';

CREATE TABLE IF NOT EXISTS `sys_user_mfa` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID，用户级 MFA 使用 0',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `secret_ciphertext` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'TOTP 密钥密文',
  `enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已确认启用：0=否，1=是',
  `recovery_codes_ciphertext` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '一次性恢复码密文',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_mfa_user` (`tenant_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户 MFA 密钥';

SET @db := DATABASE();

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `pwd_update_time` datetime NULL DEFAULT NULL COMMENT ''最近一次口令更新时间'' AFTER `superior_user_id`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'pwd_update_time'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `must_change_pwd` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否必须下次登录修改口令：0=否，1=是'' AFTER `pwd_update_time`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'must_change_pwd'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `mfa_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否已启用 MFA：0=否，1=是'' AFTER `must_change_pwd`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'mfa_enabled'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `security_level` int NOT NULL DEFAULT 0 COMMENT ''用户密级'' AFTER `mfa_enabled`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'security_level'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `security_label` int NOT NULL DEFAULT 0 COMMENT ''用户档案安全标记'' AFTER `security_level`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'security_label'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 字段加密后密文长度超过原 phone(20)/email(100)，扩容到 512。
SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_user` MODIFY COLUMN `phone` varchar(512) NULL DEFAULT NULL COMMENT ''手机号'', MODIFY COLUMN `email` varchar(512) NULL DEFAULT NULL COMMENT ''邮箱''',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db
    AND table_name = 'sys_user'
    AND column_name = 'phone'
    AND character_maximum_length < 512
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 已有环境重复执行时刷新中文注释，避免客户端编码导致备注变成问号。
ALTER TABLE `sys_user`
  MODIFY COLUMN `pwd_update_time` datetime NULL DEFAULT NULL COMMENT '最近一次口令更新时间',
  MODIFY COLUMN `must_change_pwd` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必须下次登录修改口令：0=否，1=是',
  MODIFY COLUMN `mfa_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已启用 MFA：0=否，1=是',
  MODIFY COLUMN `security_level` int NOT NULL DEFAULT 0 COMMENT '用户密级',
  MODIFY COLUMN `security_label` int NOT NULL DEFAULT 0 COMMENT '用户档案安全标记';

ALTER TABLE `sys_user_password_history` COMMENT='用户口令历史';
ALTER TABLE `sys_user_mfa` COMMENT='用户 MFA 密钥';

-- 人员/供应商表在本项目初始化脚本中位于管理库；仅当表存在时补密级列。
SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @db AND table_name = 'basic_employee') > 0
    AND (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db AND table_name = 'basic_employee' AND column_name = 'security_label') = 0,
    'ALTER TABLE `basic_employee` ADD COLUMN `security_label` int NOT NULL DEFAULT 0 COMMENT ''安全标记'' , ADD INDEX `idx_emp_security_label` (`security_label`)',
    'SELECT 1')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = @db AND table_name = 'basic_supplier') > 0
    AND (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @db AND table_name = 'basic_supplier' AND column_name = 'security_label') = 0,
    'ALTER TABLE `basic_supplier` ADD COLUMN `security_label` int NOT NULL DEFAULT 0 COMMENT ''安全标记'' , ADD INDEX `idx_supplier_security_label` (`security_label`)',
    'SELECT 1')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------------
-- 2. 历史库：操作日志哈希链
-- ---------------------------------------------------------------------------
USE `forgex_history`;

SET @db := DATABASE();

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `sys_operation_log` ADD COLUMN `success` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''操作是否成功：0=失败，1=成功'' AFTER `response_status`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_operation_log' AND column_name = 'success'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `sys_operation_log` ADD COLUMN `record_hash` char(64) NULL DEFAULT NULL COMMENT ''本条审计记录 SM3 哈希'' AFTER `success`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_operation_log' AND column_name = 'record_hash'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `sys_operation_log` ADD COLUMN `prev_hash` char(64) NULL DEFAULT NULL COMMENT ''上一条审计记录 SM3 哈希'' AFTER `record_hash`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_operation_log' AND column_name = 'prev_hash'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------------
-- 3. 校验登录依赖列
-- ---------------------------------------------------------------------------
USE `forgex_admin`;

SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'sys_user'
  AND column_name IN ('pwd_update_time', 'must_change_pwd', 'mfa_enabled', 'security_level', 'security_label')
ORDER BY ORDINAL_POSITION;
