-- 等保三级软件层整改 schema 回滚
-- 适用库：forgex_admin、forgex_history
-- 说明：删除本次补齐的登出审计/MFA/密级字段与新表。执行前请确认无业务依赖这些列。

USE `forgex_history`;

SET @db := DATABASE();
SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_operation_log` DROP COLUMN `prev_hash`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_operation_log' AND column_name = 'prev_hash'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_operation_log` DROP COLUMN `record_hash`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_operation_log' AND column_name = 'record_hash'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_operation_log` DROP COLUMN `success`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_operation_log' AND column_name = 'success'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

USE `forgex_admin`;

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @db AND table_name = 'basic_supplier' AND index_name = 'idx_supplier_security_label') > 0,
    'ALTER TABLE `basic_supplier` DROP INDEX `idx_supplier_security_label`',
    'SELECT 1')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `basic_supplier` DROP COLUMN `security_label`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'basic_supplier' AND column_name = 'security_label'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @db AND table_name = 'basic_employee' AND index_name = 'idx_emp_security_label') > 0,
    'ALTER TABLE `basic_employee` DROP INDEX `idx_emp_security_label`',
    'SELECT 1')
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `basic_employee` DROP COLUMN `security_label`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'basic_employee' AND column_name = 'security_label'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_user` DROP COLUMN `security_label`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'security_label'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_user` DROP COLUMN `security_level`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'security_level'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_user` DROP COLUMN `mfa_enabled`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'mfa_enabled'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_user` DROP COLUMN `must_change_pwd`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'must_change_pwd'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `sys_user` DROP COLUMN `pwd_update_time`',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db AND table_name = 'sys_user' AND column_name = 'pwd_update_time'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

DROP TABLE IF EXISTS `sys_user_mfa`;
DROP TABLE IF EXISTS `sys_user_password_history`;
