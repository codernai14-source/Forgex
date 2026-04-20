-- ============================================
-- 打印记录表格配置脚本
-- 目标数据库：forgex_common
-- 表：fx_table_config, fx_table_column_config
-- ============================================

-- 1. 插入表格主配置 (tenant_id = 0 表示公共配置)
INSERT INTO fx_table_config (tenant_id, create_time, create_by, update_time, update_by, deleted, table_code, table_name_i18n_json, table_type, row_key, default_page_size, default_sort_json, enabled, version)
VALUES (0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', '{"zh-CN":"打印记录","en-US":"Print Record"}', 'NORMAL', 'id', 20, NULL, true, 1);

-- 2. 插入表格列配置
-- 由于需要获取刚插入的 config id，先查询
SET @config_id = (SELECT id FROM fx_table_config WHERE table_code = 'label_print_record_table' AND tenant_id = 0 LIMIT 1);

INSERT INTO fx_table_column_config (tenant_id, create_time, create_by, update_time, update_by, deleted, table_code, field, title_i18n_json, align, width, fixed, ellipsis, sortable, sorter_field, queryable, query_type, query_operator, dict_code, render_type, perm_key, order_num, enabled)
VALUES
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'printNo', '{"zh-CN":"打印流水号","en-US":"Print No"}', 'center', 180, NULL, false, true, 'print_no', true, 'input', 'like', NULL, NULL, NULL, 1, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'templateName', '{"zh-CN":"模板名称","en-US":"Template Name"}', 'center', 150, NULL, true, false, NULL, true, 'input', 'like', NULL, NULL, NULL, 2, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'templateType', '{"zh-CN":"模板类型","en-US":"Template Type"}', 'center', 120, NULL, false, true, NULL, true, 'select', NULL, 'template_type', NULL, NULL, 3, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'barcodeNo', '{"zh-CN":"条码号","en-US":"Barcode No"}', 'center', 160, NULL, true, true, 'barcode_no', true, 'input', 'like', NULL, NULL, NULL, 4, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'materialCode', '{"zh-CN":"物料编码","en-US":"Material Code"}', 'center', 130, NULL, false, true, 'material_code', true, 'input', 'like', NULL, NULL, NULL, 5, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'materialName', '{"zh-CN":"物料名称","en-US":"Material Name"}', 'center', 150, NULL, true, false, NULL, true, 'input', 'like', NULL, NULL, NULL, 6, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'lotNo', '{"zh-CN":"LOT号","en-US":"LOT No"}', 'center', 130, NULL, false, true, 'lot_no', true, 'input', 'like', NULL, NULL, NULL, 7, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'batchNo', '{"zh-CN":"批次号","en-US":"Batch No"}', 'center', 130, NULL, false, true, 'batch_no', true, 'input', 'like', NULL, NULL, NULL, 8, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'printCount', '{"zh-CN":"打印张数","en-US":"Print Count"}', 'center', 100, NULL, false, true, NULL, false, NULL, NULL, NULL, NULL, NULL, 9, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'operatorName', '{"zh-CN":"操作人","en-US":"Operator"}', 'center', 100, NULL, false, false, NULL, false, NULL, NULL, NULL, NULL, NULL, 10, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'printTime', '{"zh-CN":"打印时间","en-US":"Print Time"}', 'center', 170, NULL, false, true, 'print_time', true, 'dateRange', NULL, NULL, NULL, NULL, 11, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'printType', '{"zh-CN":"打印类型","en-US":"Print Type"}', 'center', 100, NULL, false, true, NULL, true, 'select', NULL, 'print_type', NULL, NULL, 12, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'remark', '{"zh-CN":"备注","en-US":"Remark"}', 'center', 150, NULL, true, false, NULL, false, NULL, NULL, NULL, NULL, NULL, 13, true),
(0, NOW(), 'system', NOW(), 'system', false, 'label_print_record_table', 'action', '{"zh-CN":"操作","en-US":"Action"}', 'center', 140, 'right', false, false, NULL, false, NULL, NULL, NULL, NULL, NULL, 14, true);
