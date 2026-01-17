-- UserTable 表格配置去重（物理删除，仅保留一套）
-- 目标：
-- 1) fx_table_config: table_code='UserTable' 只保留 tenant_id=0 一条
-- 2) fx_table_column_config: table_code='UserTable' 每个 field 只保留一条
--    - 优先保留 title_i18n_json 含 "zh-CN" 的记录

DELETE FROM fx_table_config
WHERE table_code='UserTable'
  AND tenant_id <> 0;

DELETE FROM fx_table_column_config
WHERE table_code='UserTable'
  AND deleted=0
  AND id NOT IN (
    SELECT keep_id FROM (
      SELECT COALESCE(
        MIN(CASE WHEN JSON_EXTRACT(title_i18n_json, '$.\"zh-CN\"') IS NOT NULL THEN id END),
        MIN(id)
      ) AS keep_id
      FROM fx_table_column_config
      WHERE table_code='UserTable' AND deleted=0
      GROUP BY field
    ) t
  );

