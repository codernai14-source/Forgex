-- ============================================================
-- Fix action column i18n title for import/export template config
-- Date: 2026-04-08
-- Scope:
--   1) ExcelImportConfigTable action column
--   2) ExcelExportConfigTable action column
-- ============================================================

SET NAMES utf8mb4;

UPDATE fx_table_column_config
SET
  title_i18n_json = JSON_OBJECT(
    'zh-CN', '操作',
    'en-US', 'Action',
    'zh-TW', '操作',
    'ja-JP', '操作',
    'ko-KR', '작업'
  ),
  update_time = NOW(),
  update_by = 'script:20260408_fix_excel_action_i18n'
WHERE table_code IN ('ExcelImportConfigTable', 'ExcelExportConfigTable')
  AND field = 'action'
  AND deleted = 0;

-- Verify result
SELECT
  table_code,
  field,
  title_i18n_json
FROM fx_table_column_config
WHERE table_code IN ('ExcelImportConfigTable', 'ExcelExportConfigTable')
  AND field = 'action'
  AND deleted = 0;
