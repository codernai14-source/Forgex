/*
  Forgex five-language i18n repair
  Date: 2026-05-08
  Target databases: forgex_admin, forgex_common
  Purpose:
    - Fill missing zh-CN/en-US/zh-TW/ja-JP/ko-KR keys in existing i18n JSON columns.
    - Keep existing translations untouched.
    - Do not create or alter tables.

  Before executing, export affected rows:
    mysqldump -h127.0.0.1 -uroot -p123456 --default-character-set=utf8mb4 forgex_admin sys_menu sys_c_menu sys_dict > forgex_admin_i18n_backup_20260508.sql
    mysqldump -h127.0.0.1 -uroot -p123456 --default-character-set=utf8mb4 forgex_common fx_i18n_message fx_table_config fx_table_column_config sys_response_message_template > forgex_common_i18n_backup_20260508.sql
*/

SET NAMES utf8mb4;

USE forgex_admin;

UPDATE sys_menu
SET name_i18n_json = JSON_SET(
  CASE WHEN JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')) THEN CAST(COALESCE(NULLIF(name_i18n_json, ''), '{}') AS JSON) ELSE JSON_OBJECT() END,
  '$."zh-CN"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."zh-CN"')), NULL), ''), name),
  '$."en-US"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."en-US"')), NULL), ''), name),
  '$."zh-TW"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."zh-TW"')), NULL), ''), name),
  '$."ja-JP"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."ja-JP"')), NULL), ''), name),
  '$."ko-KR"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."ko-KR"')), NULL), ''), name)
)
WHERE name IS NOT NULL AND name <> '';

UPDATE sys_c_menu
SET name_i18n_json = JSON_SET(
  CASE WHEN JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')) THEN CAST(COALESCE(NULLIF(name_i18n_json, ''), '{}') AS JSON) ELSE JSON_OBJECT() END,
  '$."zh-CN"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."zh-CN"')), NULL), ''), name),
  '$."en-US"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."en-US"')), NULL), ''), name),
  '$."zh-TW"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."zh-TW"')), NULL), ''), name),
  '$."ja-JP"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."ja-JP"')), NULL), ''), name),
  '$."ko-KR"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(name_i18n_json, ''), '{}'), '$."ko-KR"')), NULL), ''), name)
)
WHERE name IS NOT NULL AND name <> '';

UPDATE sys_dict
SET dict_value_i18n_json = JSON_SET(
  CASE WHEN JSON_VALID(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}')) THEN CAST(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}') AS JSON) ELSE JSON_OBJECT() END,
  '$."zh-CN"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}'), '$."zh-CN"')), NULL), ''), dict_value),
  '$."en-US"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}'), '$."en-US"')), NULL), ''), dict_value),
  '$."zh-TW"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}'), '$."zh-TW"')), NULL), ''), dict_value),
  '$."ja-JP"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}'), '$."ja-JP"')), NULL), ''), dict_value),
  '$."ko-KR"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(dict_value_i18n_json, ''), '{}'), '$."ko-KR"')), NULL), ''), dict_value)
)
WHERE dict_value IS NOT NULL AND dict_value <> '';

USE forgex_common;

UPDATE fx_i18n_message
SET text_i18n_json = JSON_SET(
  CASE WHEN JSON_VALID(COALESCE(NULLIF(text_i18n_json, ''), '{}')) THEN CAST(COALESCE(NULLIF(text_i18n_json, ''), '{}') AS JSON) ELSE JSON_OBJECT() END,
  '$."zh-CN"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(text_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(text_i18n_json, ''), '{}'), '$."zh-CN"')), NULL), ''), prompt_code),
  '$."en-US"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(text_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(text_i18n_json, ''), '{}'), '$."en-US"')), NULL), ''), prompt_code),
  '$."zh-TW"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(text_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(text_i18n_json, ''), '{}'), '$."zh-TW"')), NULL), ''), prompt_code),
  '$."ja-JP"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(text_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(text_i18n_json, ''), '{}'), '$."ja-JP"')), NULL), ''), prompt_code),
  '$."ko-KR"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(text_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(text_i18n_json, ''), '{}'), '$."ko-KR"')), NULL), ''), prompt_code)
)
WHERE prompt_code IS NOT NULL AND prompt_code <> '';

UPDATE fx_table_config
SET table_name_i18n_json = JSON_SET(
  CASE WHEN JSON_VALID(COALESCE(NULLIF(table_name_i18n_json, ''), '{}')) THEN CAST(COALESCE(NULLIF(table_name_i18n_json, ''), '{}') AS JSON) ELSE JSON_OBJECT() END,
  '$."zh-CN"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(table_name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(table_name_i18n_json, ''), '{}'), '$."zh-CN"')), NULL), ''), table_code),
  '$."en-US"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(table_name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(table_name_i18n_json, ''), '{}'), '$."en-US"')), NULL), ''), table_code),
  '$."zh-TW"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(table_name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(table_name_i18n_json, ''), '{}'), '$."zh-TW"')), NULL), ''), table_code),
  '$."ja-JP"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(table_name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(table_name_i18n_json, ''), '{}'), '$."ja-JP"')), NULL), ''), table_code),
  '$."ko-KR"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(table_name_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(table_name_i18n_json, ''), '{}'), '$."ko-KR"')), NULL), ''), table_code)
)
WHERE table_code IS NOT NULL AND table_code <> '';

UPDATE fx_table_column_config
SET title_i18n_json = JSON_SET(
  CASE WHEN JSON_VALID(COALESCE(NULLIF(title_i18n_json, ''), '{}')) THEN CAST(COALESCE(NULLIF(title_i18n_json, ''), '{}') AS JSON) ELSE JSON_OBJECT() END,
  '$."zh-CN"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(title_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(title_i18n_json, ''), '{}'), '$."zh-CN"')), NULL), ''), field),
  '$."en-US"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(title_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(title_i18n_json, ''), '{}'), '$."en-US"')), NULL), ''), field),
  '$."zh-TW"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(title_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(title_i18n_json, ''), '{}'), '$."zh-TW"')), NULL), ''), field),
  '$."ja-JP"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(title_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(title_i18n_json, ''), '{}'), '$."ja-JP"')), NULL), ''), field),
  '$."ko-KR"', COALESCE(NULLIF(IF(JSON_VALID(COALESCE(NULLIF(title_i18n_json, ''), '{}')), JSON_UNQUOTE(JSON_EXTRACT(COALESCE(NULLIF(title_i18n_json, ''), '{}'), '$."ko-KR"')), NULL), ''), field)
)
WHERE field IS NOT NULL AND field <> '';

SELECT 'forgex_admin.sys_menu missing json lang rows' AS check_name, COUNT(*) AS missing_count
FROM forgex_admin.sys_menu
WHERE name_i18n_json IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(name_i18n_json, '$."zh-CN"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(name_i18n_json, '$."en-US"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(name_i18n_json, '$."zh-TW"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(name_i18n_json, '$."ja-JP"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(name_i18n_json, '$."ko-KR"')) IS NULL;

SELECT 'forgex_common.fx_table_column_config missing json lang rows' AS check_name, COUNT(*) AS missing_count
FROM forgex_common.fx_table_column_config
WHERE title_i18n_json IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(title_i18n_json, '$."zh-CN"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(title_i18n_json, '$."en-US"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(title_i18n_json, '$."zh-TW"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(title_i18n_json, '$."ja-JP"')) IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(title_i18n_json, '$."ko-KR"')) IS NULL;
