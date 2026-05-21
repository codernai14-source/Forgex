SET NAMES utf8mb4;
USE forgex_integration;

SET @schema_name := DATABASE();

SET @has_token_expire_type := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'fx_third_authorization'
    AND column_name = 'token_expire_type'
);
SET @sql := IF(
  @has_token_expire_type = 0,
  'ALTER TABLE fx_third_authorization ADD COLUMN token_expire_type varchar(32) NULL COMMENT ''Token有效期类型: HOURS/DAY/MONTH/YEAR/CUSTOM/FOREVER'' AFTER token_value',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_token_expire_value := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = @schema_name
    AND table_name = 'fx_third_authorization'
    AND column_name = 'token_expire_value'
);
SET @sql := IF(
  @has_token_expire_value = 0,
  'ALTER TABLE fx_third_authorization ADD COLUMN token_expire_value int NULL COMMENT ''Token有效期数值'' AFTER token_expire_type',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @third_system_id := (
  SELECT id
  FROM fx_third_system
  WHERE system_code = 'cs'
    AND deleted = 0
  LIMIT 1
);

UPDATE fx_api_config
SET auth_type = 'TOKEN',
    auth_config = JSON_OBJECT('thirdSystemId', @third_system_id)
WHERE api_code = 'sys_user_third_party_inbound'
  AND deleted = 0
  AND @third_system_id IS NOT NULL;

SELECT id, api_code, auth_type, auth_config
FROM fx_api_config
WHERE api_code = 'sys_user_third_party_inbound'
  AND deleted = 0;
