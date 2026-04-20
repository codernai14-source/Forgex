-- ============================================
-- 标签打印菜单配置脚本（根据实体类结构）
-- 目标数据库：forgex_admin
-- 表：sys_menu, sys_role_menu
-- 用途：配置标签打印页面的菜单和权限
-- ============================================

-- 1. 检查标签管理父菜单是否存在
SET @label_menu_id = (SELECT id FROM sys_menu WHERE path = 'label' AND parent_id = 0 LIMIT 1);

-- 如果不存在，创建标签管理父菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, 0, 'MENU', 1, 'label', '标签管理', '{"zh-CN":"标签管理","en-US":"Label Management"}', 'TagOutlined', NULL, 'label:manage', 'embedded', NULL, 15, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'label' AND parent_id = 0);

-- 获取标签管理父菜单ID
SET @label_menu_id = (SELECT id FROM sys_menu WHERE path = 'label' AND parent_id = 0 LIMIT 1);

-- 2. 检查标签打印子菜单是否存在
SET @print_menu_id = (SELECT id FROM sys_menu WHERE path = 'print' AND parent_id = @label_menu_id LIMIT 1);

-- 如果不存在，创建标签打印子菜单
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @label_menu_id, 'MENU', 2, 'print', '标签打印', '{"zh-CN":"标签打印","en-US":"Label Print"}', 'PrinterOutlined', 'LabelPrint', 'label:print:manage', 'embedded', NULL, 3, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'print' AND parent_id = @label_menu_id);

-- 获取标签打印子菜单ID
SET @print_menu_id = (SELECT id FROM sys_menu WHERE path = 'print' AND parent_id = @label_menu_id LIMIT 1);

-- 3. 为标签打印菜单添加按钮权限
-- 预览按钮
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @print_menu_id, 'BUTTON', 3, 'preview', '预览', '{"zh-CN":"预览","en-US":"Preview"}', NULL, NULL, 'label:print:query', 'embedded', NULL, 1, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'preview' AND parent_id = @print_menu_id);

-- 打印按钮
INSERT INTO sys_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, type, menu_level, path, name, name_i18n_json, icon, component_key, perm_key, menu_mode, external_url, order_num, visible, status, tenant_type)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @print_menu_id, 'BUTTON', 3, 'execute', '打印', '{"zh-CN":"打印","en-US":"Print"}', NULL, NULL, 'label:print:execute', 'embedded', NULL, 2, true, true, 'PUBLIC'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'execute' AND parent_id = @print_menu_id);

-- 4. 为admin角色分配标签打印菜单权限
-- 获取admin角色ID
SET @admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'admin' LIMIT 1);

-- 分配菜单权限
INSERT INTO sys_role_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, role_id, menu_id)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @admin_role_id, @print_menu_id
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = @print_menu_id);

-- 分配按钮权限
INSERT INTO sys_role_menu (tenant_id, create_time, create_by, update_time, update_by, deleted, role_id, menu_id)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @admin_role_id, id
FROM sys_menu
WHERE parent_id = @print_menu_id
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = @admin_role_id AND menu_id = sys_menu.id);

SELECT '标签打印菜单配置完成！' AS result;
