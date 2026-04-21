-- ============================================
-- 标签模块菜单配置脚本
-- 目标数据库：forgex_admin
-- 表：sys_module, sys_menu, sys_role_menu
-- ============================================

-- 1. 查询或创建标签模块 (module_code = 'label')
INSERT INTO sys_module (tenant_id, create_time, create_by, update_time, update_by, deleted, module_code, module_name, module_name_i18n_json, icon, order_num)
SELECT 0, NOW(), 'system', NOW(), 'system', false, 'label', '标签模块', '{"zh-CN":"标签模块","en-US":"Label Module"}', 'TagOutlined', 30
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE module_code = 'label');

-- 获取标签模块ID
SET @label_module_id = (SELECT id FROM sys_module WHERE module_code = 'label' LIMIT 1);

-- 2. 查询基础信息模块ID (module_code = 'basic')
SET @basic_module_id = (SELECT id FROM sys_module WHERE module_code = 'basic' LIMIT 1);

-- 3. 创建标签管理的父菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, 0, 'menu', 1, 'label', '标签管理', '{"zh-CN":"标签管理","en-US":"Label Management"}', 'TagOutlined', 'BasicLabel', 'basic:label:view', 'embedded', NULL, 20, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'label' AND module_id = @basic_module_id AND parent_id = 0);

-- 获取标签管理父菜单ID
SET @label_parent_id = (SELECT id FROM sys_menu WHERE path = 'label' AND module_id = @basic_module_id AND parent_id = 0 LIMIT 1);

-- 4. 创建标签模板子菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @label_parent_id, 'menu', 2, 'label/template', '标签模板', '{"zh-CN":"标签模板","en-US":"Label Template"}', 'FileTextOutlined', 'LabelTemplate', 'label:template:view', 'embedded', NULL, 10, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'label/template' AND module_id = @basic_module_id);

-- 5. 创建打印记录子菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @label_parent_id, 'menu', 2, 'label/record', '打印记录', '{"zh-CN":"打印记录","en-US":"Print Record"}', 'PrinterOutlined', 'LabelRecord', 'label:record:view', 'embedded', NULL, 20, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'label/record' AND module_id = @basic_module_id);

-- 6. 创建标签打印子菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @label_parent_id, 'menu', 2, 'label/print', '标签打印', '{"zh-CN":"标签打印","en-US":"Label Print"}', 'PrintOutlined', 'LabelPrint', 'label:print:view', 'embedded', NULL, 30, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'label/print' AND module_id = @basic_module_id);

-- 7. 创建标签绑定子菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @label_parent_id, 'menu', 2, 'label/binding', '标签绑定', '{"zh-CN":"标签绑定","en-US":"Label Binding"}', 'LinkOutlined', 'LabelBinding', 'label:binding:view', 'embedded', NULL, 40, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'label/binding' AND module_id = @basic_module_id);

-- 8. 创建标签模板的按钮权限
SET @template_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/template' AND module_id = @basic_module_id LIMIT 1);

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @template_menu_id, 'button', 3, NULL, '新增模板', '{"zh-CN":"新增模板","en-US":"Add Template"}', NULL, 'label:template:add', 1, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:template:add');

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @template_menu_id, 'button', 3, NULL, '编辑模板', '{"zh-CN":"编辑模板","en-US":"Edit Template"}', NULL, 'label:template:edit', 2, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:template:edit');

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @template_menu_id, 'button', 3, NULL, '删除模板', '{"zh-CN":"删除模板","en-US":"Delete Template"}', NULL, 'label:template:delete', 3, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:template:delete');

-- 9. 创建打印记录的按钮权限
SET @record_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/record' AND module_id = @basic_module_id LIMIT 1);

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @record_menu_id, 'button', 3, NULL, '查看详情', '{"zh-CN":"查看详情","en-US":"View Detail"}', NULL, 'label:record:query', 1, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:record:query');

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @record_menu_id, 'button', 3, NULL, '补打', '{"zh-CN":"补打","en-US":"Reprint"}', NULL, 'label:print:reprint', 2, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:print:reprint');

-- 10. 创建标签打印的按钮权限
SET @print_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/print' AND module_id = @basic_module_id LIMIT 1);

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @print_menu_id, 'button', 3, NULL, '打印', '{"zh-CN":"打印","en-US":"Print"}', NULL, 'label:print:add', 1, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:print:add');

-- 11. 创建标签绑定的按钮权限
SET @binding_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/binding' AND module_id = @basic_module_id LIMIT 1);

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @binding_menu_id, 'button', 3, NULL, '新增绑定', '{"zh-CN":"新增绑定","en-US":"Add Binding"}', NULL, 'label:binding:add', 1, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:binding:add');

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @binding_menu_id, 'button', 3, NULL, '编辑绑定', '{"zh-CN":"编辑绑定","en-US":"Edit Binding"}', NULL, 'label:binding:edit', 2, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:binding:edit');

INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, module_id, parent_id, type, menu_level, path, name, name_i18n_json, icon, perm_key, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @basic_module_id, @binding_menu_id, 'button', 3, NULL, '删除绑定', '{"zh-CN":"删除绑定","en-US":"Delete Binding"}', NULL, 'label:binding:delete', 3, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm_key = 'label:binding:delete');

-- 12. 给admin角色分配标签模块菜单权限
-- 查询admin角色ID
SET @admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'admin' LIMIT 1);

-- 获取所有标签相关菜单ID
SET @label_menu_id = (SELECT id FROM sys_menu WHERE path = 'label' AND module_id = @basic_module_id LIMIT 1);
SET @template_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/template' AND module_id = @basic_module_id LIMIT 1);
SET @record_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/record' AND module_id = @basic_module_id LIMIT 1);
SET @print_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/print' AND module_id = @basic_module_id LIMIT 1);
SET @binding_menu_id = (SELECT id FROM sys_menu WHERE path = 'label/binding' AND module_id = @basic_module_id LIMIT 1);

-- 分配菜单权限
INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 0, @admin_role_id, @label_menu_id
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @label_menu_id);

INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 0, @admin_role_id, @template_menu_id
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @template_menu_id);

INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 0, @admin_role_id, @record_menu_id
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @record_menu_id);

INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 0, @admin_role_id, @print_menu_id
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @print_menu_id);

INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 0, @admin_role_id, @binding_menu_id
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @binding_menu_id);

SELECT '标签模块菜单配置完成！' AS result;
