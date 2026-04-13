-- 前端乱码配置定向修复 SQL
-- 执行前请先运行诊断 SQL，确认需要修复的范围。

START TRANSACTION;

INSERT INTO forgex_common.fx_table_config (
  tenant_id,
  table_code,
  table_name_i18n_json,
  table_type,
  row_key,
  default_page_size,
  enabled,
  version,
  create_by,
  create_time,
  update_by,
  update_time,
  deleted
)
SELECT
  0,
  'I18nLanguageTypeTable',
  '{"zh-CN":"语言配置","en-US":"Language Configuration","ja-JP":"言語設定","ko-KR":"언어 설정","zh-TW":"語言配置"}',
  'NORMAL',
  'id',
  10,
  1,
  1,
  'manual_fix',
  NOW(),
  'manual_fix',
  NOW(),
  0
WHERE NOT EXISTS (
  SELECT 1
  FROM forgex_common.fx_table_config
  WHERE deleted = 0
    AND table_code = 'I18nLanguageTypeTable'
);

INSERT INTO forgex_common.fx_table_column_config (
  tenant_id,
  table_code,
  field,
  title_i18n_json,
  align,
  width,
  fixed,
  ellipsis,
  sortable,
  sorter_field,
  queryable,
  query_type,
  query_operator,
  dict_code,
  render_type,
  perm_key,
  order_num,
  enabled,
  create_by,
  create_time,
  update_by,
  update_time,
  deleted
)
SELECT *
FROM (
  SELECT 0, 'I18nLanguageTypeTable', 'langCode', '{"zh-CN":"语言代码","en-US":"Language Code","ja-JP":"言語コード","ko-KR":"언어 코드","zh-TW":"語言代碼"}', 'left', 120, NULL, 0, 0, NULL, 1, 'input', 'like', NULL, NULL, NULL, 1, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
  UNION ALL
  SELECT 0, 'I18nLanguageTypeTable', 'langName', '{"zh-CN":"语言名称","en-US":"Language Name","ja-JP":"言語名","ko-KR":"언어명","zh-TW":"語言名稱"}', 'left', 150, NULL, 0, 0, NULL, 1, 'input', 'like', NULL, NULL, NULL, 2, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
  UNION ALL
  SELECT 0, 'I18nLanguageTypeTable', 'langNameEn', '{"zh-CN":"英文名称","en-US":"English Name","ja-JP":"英語名","ko-KR":"영문명","zh-TW":"英文名稱"}', 'left', 150, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 3, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
  UNION ALL
  SELECT 0, 'I18nLanguageTypeTable', 'icon', '{"zh-CN":"图标","en-US":"Icon","ja-JP":"アイコン","ko-KR":"아이콘","zh-TW":"圖示"}', 'center', 100, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 4, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
  UNION ALL
  SELECT 0, 'I18nLanguageTypeTable', 'orderNum', '{"zh-CN":"排序号","en-US":"Order","ja-JP":"並び順","ko-KR":"정렬","zh-TW":"排序號"}', 'center', 90, NULL, 0, 1, 'order_num', 0, NULL, NULL, NULL, NULL, NULL, 5, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
  UNION ALL
  SELECT 0, 'I18nLanguageTypeTable', 'enabled', '{"zh-CN":"是否启用","en-US":"Enabled","ja-JP":"有効","ko-KR":"사용 여부","zh-TW":"是否啟用"}', 'center', 100, NULL, 0, 0, NULL, 1, 'select', 'eq', NULL, NULL, NULL, 6, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
  UNION ALL
  SELECT 0, 'I18nLanguageTypeTable', 'isDefault', '{"zh-CN":"默认语言","en-US":"Default Language","ja-JP":"既定言語","ko-KR":"기본 언어","zh-TW":"預設語言"}', 'center', 120, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 7, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
  UNION ALL
  SELECT 0, 'I18nLanguageTypeTable', 'createTime', '{"zh-CN":"创建时间","en-US":"Create Time","ja-JP":"作成時間","ko-KR":"생성 시간","zh-TW":"創建時間"}', 'center', 180, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 8, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
  UNION ALL
  SELECT 0, 'I18nLanguageTypeTable', 'action', '{"zh-CN":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업","zh-TW":"操作"}', 'center', 200, 'right', 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 9, 1, 'manual_fix', NOW(), 'manual_fix', NOW(), 0
) AS seed (
  tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis, sortable, sorter_field,
  queryable, query_type, query_operator, dict_code, render_type, perm_key, order_num, enabled,
  create_by, create_time, update_by, update_time, deleted
)
WHERE NOT EXISTS (
  SELECT 1
  FROM forgex_common.fx_table_column_config c
  WHERE c.deleted = 0
    AND c.table_code = seed.table_code
    AND c.field = seed.field
);

COMMIT;
