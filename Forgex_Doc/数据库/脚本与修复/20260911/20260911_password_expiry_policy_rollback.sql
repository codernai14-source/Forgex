-- 密码有效期策略回滚（仅移除本次新增字段）
SET @db_name = DATABASE();
SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@db_name AND table_name='sys_user' AND column_name='pwd_update_time');
SET @sql = IF(@column_exists=1, 'ALTER TABLE sys_user DROP COLUMN pwd_update_time', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
