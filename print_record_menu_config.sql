-- ============================================
-- 打印记录菜单配置脚本
-- 目标数据库：forgex_admin
-- 表：sys_menu, sys_role_menu
-- 用途：配置打印记录页面的菜单和权限
-- ============================================

-- 1. 查询基础信息模块ID (module_code = 'basic')
SET @basic_module_id = (SELECT id FROM sys_module WHERE module_code = 'basic' LIMIT 1);

-- 2. 查询或创建标签管理父菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, 0, 'menu', 1, 'label', '标签管理', '{"zh-CN":"标签管理","en-US":"Label Management"}', 'TagOutlined', 'BasicLabel', 'basic:label:view', 'embedded', NULL, 20, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'label' AND module_id = @basic_module_id AND parent_id = 0);

-- 获取标签管理父菜单ID
SET @label_parent_id = (SELECT id FROM sys_menu WHERE path = 'label' AND module_id = @basic_module_id AND parent_id = 0 LIMIT 1);

-- 3. 创建打印记录子菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @label_parent_id, 'menu', 2, 'label/record', '打印记录', '{"zh-CN":"打印记录","en-US":"Print Record"}', 'PrinterOutlined', 'LabelRecord', 'label:record:view', 'embedded', NULL, 20, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'label/record' AND module_id = @basic_module_id);

-- 4. 创建打印记录的按钮权限
SET @record_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/record' AND module_id = @basic_module_id LIMIT 1);

-- 查看详情按钮
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @record_menu_id, 'button', 3, NULL, '查看详情', '{"zh-CN":"查看详情","en-US":"View Detail"}', NULL, 'label:record:query', 1, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:record:query');

-- 补打按钮
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @record_menu_id, 'button', 3, NULL, '补打', '{"zh-CN":"补打","en-US":"Reprint"}', NULL, 'label:print:reprint', 2, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:print:reprint');

-- 5. 给admin角色分配打印记录菜单权限
-- 查询admin角色ID
SET @admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'admin' LIMIT 1);

-- 获取打印记录菜单ID
SET @record_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/record' AND module_id = @basic_module_id LIMIT 1);

-- 分配菜单权限
INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 0, @admin_role_id, @record_menu_id
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @record_menu_id);

-- 分配按钮权限
INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 0, @admin_role_id, id
FROM sys_menu
WHERE perm_key IN ('label:record:query', 'label:print:reprint')
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = sys_menu.id);

SELECT '打印记录菜单配置完成！' AS result;
