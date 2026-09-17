-- 密码有效期策略（幂等升级）
SET @db_name = DATABASE();
SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db_name AND table_name='sys_user' AND column_name='pwd_update_time');
SET @sql = IF(@column_exists=0, 'ALTER TABLE sys_user ADD COLUMN pwd_update_time datetime NULL COMMENT ''密码最后修改时间''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
UPDATE sys_user SET pwd_update_time = COALESCE(pwd_update_time, update_time, create_time, NOW());
UPDATE sys_config SET config_value = JSON_SET(COALESCE(config_value, '{}'), '$.expireEnabled', COALESCE(JSON_EXTRACT(config_value, '$.expireEnabled'), false), '$.expireDays', COALESCE(JSON_EXTRACT(config_value, '$.expireDays'), 90), '$.expireWarnDays', COALESCE(JSON_EXTRACT(config_value, '$.expireWarnDays'), 7)) WHERE config_key='security.password.policy';
