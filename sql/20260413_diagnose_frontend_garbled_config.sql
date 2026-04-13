-- 前端乱码相关配置诊断 SQL
-- 目标：只定位可疑配置数据，不直接修改主业务表

SELECT id, table_code, table_name_i18n_json, enabled, version
FROM forgex_common.fx_table_config
WHERE deleted = 0
  AND (
    table_code IN ('DictTable', 'I18nLanguageTypeTable')
    OR table_name_i18n_json LIKE '%�%'
    OR table_name_i18n_json LIKE '%?%'
    OR table_name_i18n_json REGEXP '鏉|鍒|璇|閰|缂|鐎|闄|琛'
  )
ORDER BY id;

SELECT id, table_code, field, title_i18n_json, enabled, order_num
FROM forgex_common.fx_table_column_config
WHERE deleted = 0
  AND (
    table_code IN ('DictTable', 'I18nLanguageTypeTable')
    OR title_i18n_json LIKE '%�%'
    OR title_i18n_json LIKE '%?%'
    OR title_i18n_json REGEXP '鏉|鍒|璇|閰|缂|鐎|闄|琛'
  )
ORDER BY table_code, order_num, id;

SELECT id, dict_code, dict_name, dict_value_i18n_json
FROM forgex_admin.sys_dict
WHERE deleted = 0
  AND (
    dict_name LIKE '%�%'
    OR dict_name LIKE '%?%'
    OR dict_value_i18n_json LIKE '%�%'
    OR dict_value_i18n_json REGEXP '鏉|鍒|璇|閰|缂|鐎|闄|琛'
  )
ORDER BY id;

SELECT id, name, name_i18n_json
FROM forgex_admin.sys_menu
WHERE deleted = 0
  AND (
    name LIKE '%�%'
    OR name LIKE '%?%'
    OR CAST(name_i18n_json AS CHAR) LIKE '%�%'
    OR CAST(name_i18n_json AS CHAR) REGEXP '鏉|鍒|璇|閰|缂|鐎|闄|琛'
  )
ORDER BY id;

SELECT id, code, name, name_i18n_json
FROM forgex_admin.sys_module
WHERE deleted = 0
  AND (
    name LIKE '%�%'
    OR name LIKE '%?%'
    OR CAST(name_i18n_json AS CHAR) LIKE '%�%'
    OR CAST(name_i18n_json AS CHAR) REGEXP '鏉|鍒|璇|閰|缂|鐎|闄|琛'
  )
ORDER BY id;

SELECT id, lang_code, lang_name, lang_name_en
FROM forgex_common.fx_i18n_language_type
WHERE deleted = 0
  AND (
    lang_name LIKE '%�%'
    OR lang_name LIKE '%?%'
    OR lang_name_en LIKE '%�%'
  )
ORDER BY id;

SELECT id, module, prompt_code, text_i18n_json
FROM forgex_common.fx_i18n_message
WHERE deleted = 0
  AND (
    text_i18n_json LIKE '%�%'
    OR text_i18n_json LIKE '%?%'
    OR text_i18n_json REGEXP '鏉|鍒|璇|閰|缂|鐎|闄|琛'
  )
ORDER BY id;
