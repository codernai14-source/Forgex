-- ============================================
-- 模板绑定表格配置脚本
-- 目标数据库：forgex_common
-- 表：fx_table_config, fx_table_column_config
-- 用途：配置模板绑定页面的表格列
-- ============================================

-- 1. 先删除现有配置（如果存在）
DELETE FROM fx_table_column_config WHERE table_code = 'LabelBindingTable';
DELETE FROM fx_table_config WHERE table_code = 'LabelBindingTable';

-- 2. 插入表格主配置 (tenant_id = 0 表示公共配置)
INSERT INTO fx_table_config (tenant_id, create_time, create_by, update_time, update_by, deleted, table_code, table_name_i18n_json, table_type, row_key, default_page_size, default_sort_json, enabled, version)
VALUES (0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', '{"zh-CN":"模板绑定","en-US":"Template Binding"}', 'NORMAL', 'id', 20, NULL, true, 1);

-- 3. 插入表格列配置
INSERT INTO fx_table_column_config (tenant_id, create_time, create_by, update_time, update_by, deleted, table_code, field, title_i18n_json, align, width, fixed, ellipsis, sortable, sorter_field, queryable, query_type, query_operator, dict_code, render_type, perm_key, order_num, enabled)
VALUES
-- 1. 模板编码
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'templateCode', '{"zh-CN":"模板编码","en-US":"Template Code"}', 'center', 150, NULL, false, true, 'template_code', true, 'input', 'like', NULL, NULL, NULL, 1, true),
-- 2. 模板名称
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'templateName', '{"zh-CN":"模板名称","en-US":"Template Name"}', 'center', 150, NULL, true, false, NULL, true, 'input', 'like', NULL, NULL, NULL, 2, true),
-- 3. 绑定类型
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'bindingType', '{"zh-CN":"绑定类型","en-US":"Binding Type"}', 'center', 120, NULL, false, true, NULL, true, 'select', NULL, 'binding_type', NULL, NULL, 3, true),
-- 4. 绑定值
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'bindingValue', '{"zh-CN":"绑定值","en-US":"Binding Value"}', 'center', 150, NULL, true, true, 'binding_value', true, 'input', 'like', NULL, NULL, NULL, 4, true),
-- 5. 绑定名称
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'bindingName', '{"zh-CN":"绑定名称","en-US":"Binding Name"}', 'center', 150, NULL, true, false, NULL, true, 'input', 'like', NULL, NULL, NULL, 5, true),
-- 6. 优先级
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'priority', '{"zh-CN":"优先级","en-US":"Priority"}', 'center', 100, NULL, false, true, NULL, true, 'select', NULL, 'priority', NULL, NULL, 6, true),
-- 7. 工厂名称
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'factoryName', '{"zh-CN":"工厂","en-US":"Factory"}', 'center', 120, NULL, false, false, NULL, true, 'input', 'like', NULL, NULL, NULL, 7, true),
-- 8. 创建人
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'createBy', '{"zh-CN":"创建人","en-US":"Creator"}', 'center', 100, NULL, false, false, NULL, false, NULL, NULL, NULL, NULL, NULL, 8, true),
-- 9. 创建时间
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'createTime', '{"zh-CN":"创建时间","en-US":"Create Time"}', 'center', 170, NULL, false, true, 'create_time', true, 'dateRange', NULL, NULL, NULL, NULL, 9, true),
-- 10. 操作
(0, NOW(), 'system', NOW(), 'system', false, 'LabelBindingTable', 'action', '{"zh-CN":"操作","en-US":"Action"}', 'center', 140, 'right', false, false, NULL, false, NULL, NULL, NULL, NULL, NULL, 10, true);

SELECT '模板绑定表格配置完成！' AS result;
