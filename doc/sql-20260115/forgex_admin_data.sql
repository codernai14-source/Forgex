-- ================================================
-- Forgex Admin 数据库初始数据
-- 数据库: forgex_admin
-- 说明: 系统管理相关初始数据
-- ================================================

USE forgex_admin;

-- ================================================
-- 初始化租户数据
-- ================================================
INSERT INTO sys_tenant (tenant_name, tenant_code, description, status, create_by) VALUES 
('默认租户', 'default', '默认租户', 1, 'system');

SET @TENANT_ID = (SELECT id FROM sys_tenant WHERE tenant_code = 'default');

-- ================================================
-- 初始化管理员用户
-- 密码: 123456 (BCrypt加密)
-- ================================================
INSERT INTO sys_user (account, username, password, email, status, create_by) VALUES 
('admin', 'admin', '$2a$10$7EqJtq98hPqEX7fNLEyguOg03fZ7p.XN3okEV0H1Bs8Q5q8K9bL2u', 'admin@forgex.com', 1, 'system');

SET @ADMIN_USER_ID = (SELECT id FROM sys_user WHERE account = 'admin');

-- ================================================
-- 用户租户关联
-- ================================================
INSERT INTO sys_user_tenant (user_id, tenant_id, is_default, pref_order) VALUES 
(@ADMIN_USER_ID, @TENANT_ID, 1, 100);

-- ================================================
-- 初始化角色
-- ================================================
INSERT INTO sys_role (role_name, role_key, description, status, tenant_id, create_by) VALUES 
('系统管理员', 'admin', '系统管理员，拥有所有权限', 1, @TENANT_ID, 'system'),
('普通用户', 'user', '普通用户，基础权限', 1, @TENANT_ID, 'system'),
('部门经理', 'manager', '部门经理，部门管理权限', 1, @TENANT_ID, 'system'),
('系统审计员', 'auditor', '系统审计员，审计权限', 1, @TENANT_ID, 'system');

SET @ADMIN_ROLE_ID = (SELECT id FROM sys_role WHERE role_key = 'admin' AND tenant_id = @TENANT_ID);
SET @USER_ROLE_ID = (SELECT id FROM sys_role WHERE role_key = 'user' AND tenant_id = @TENANT_ID);
SET @MANAGER_ROLE_ID = (SELECT id FROM sys_role WHERE role_key = 'manager' AND tenant_id = @TENANT_ID);
SET @AUDITOR_ROLE_ID = (SELECT id FROM sys_role WHERE role_key = 'auditor' AND tenant_id = @TENANT_ID);

-- ================================================
-- 用户角色关联
-- ================================================
INSERT INTO sys_user_role (user_id, role_id, tenant_id) VALUES 
(@ADMIN_USER_ID, @ADMIN_ROLE_ID, @TENANT_ID);

-- ================================================
-- 初始化模块
-- ================================================
INSERT INTO sys_module (tenant_id, code, name, icon, order_num, visible, status, create_by) VALUES 
(@TENANT_ID, 'sys', '系统管理', 'SettingOutlined', 10, 1, 1, 'system');

SET @SYS_MODULE_ID = (SELECT id FROM sys_module WHERE code = 'sys' AND tenant_id = @TENANT_ID);

-- ================================================
-- 初始化菜单 - 系统管理主页
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'dashboard', '系统管理主页', 'DashboardOutlined', 'SystemDashboard', 'sys:dashboard:view', 1, 1, 1, 1, 'embedded', 'system');

SET @DASHBOARD_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'dashboard' AND type = 'menu');

-- ================================================
-- 初始化菜单 - 用户管理
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'user', '用户管理', 'UserOutlined', 'SystemUser', 'sys:user:view', 10, 1, 1, 1, 'embedded', 'system');

SET @USER_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'user' AND type = 'menu');

-- 用户管理按钮权限
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '新增用户', 'sys:user:create', 1, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '编辑用户', 'sys:user:edit', 2, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '删除用户', 'sys:user:delete', 3, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '批量删除用户', 'sys:user:batchDelete', 4, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '重置密码', 'sys:user:resetPwd', 5, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '导出用户', 'sys:user:export', 6, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '分配角色', 'sys:user:assignRole', 7, 1, 1, 2, 'embedded', 'system');

-- ================================================
-- 初始化菜单 - 角色管理
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'role', '角色管理', 'TeamOutlined', 'SystemRole', 'sys:role:view', 20, 1, 1, 1, 'embedded', 'system');

SET @ROLE_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'role' AND type = 'menu');

-- 角色管理按钮权限
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '新增角色', 'sys:role:create', 1, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '编辑角色', 'sys:role:edit', 2, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '删除角色', 'sys:role:delete', 3, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '批量删除角色', 'sys:role:batchDelete', 4, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '菜单授权', 'sys:role:authMenu', 5, 1, 1, 2, 'embedded', 'system');

-- ================================================
-- 初始化菜单 - 模块管理
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'module', '模块管理', 'AppstoreOutlined', 'SystemModule', 'sys:module:view', 30, 1, 1, 1, 'embedded', 'system');

SET @MODULE_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'module' AND type = 'menu');

-- 模块管理按钮权限
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @MODULE_MENU_ID, 'button', '新增模块', 'sys:module:create', 1, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @MODULE_MENU_ID, 'button', '编辑模块', 'sys:module:edit', 2, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @MODULE_MENU_ID, 'button', '删除模块', 'sys:module:delete', 3, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @MODULE_MENU_ID, 'button', '批量删除模块', 'sys:module:batchDelete', 4, 1, 1, 2, 'embedded', 'system');

-- ================================================
-- 初始化菜单 - 菜单管理
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'menu', '菜单管理', 'MenuOutlined', 'SystemMenu', 'sys:menu:view', 40, 1, 1, 1, 'embedded', 'system');

SET @MENU_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'menu' AND type = 'menu');

-- 菜单管理按钮权限
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @MENU_MENU_ID, 'button', '新增菜单', 'sys:menu:create', 1, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @MENU_MENU_ID, 'button', '编辑菜单', 'sys:menu:edit', 2, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @MENU_MENU_ID, 'button', '删除菜单', 'sys:menu:delete', 3, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @MENU_MENU_ID, 'button', '批量删除菜单', 'sys:menu:batchDelete', 4, 1, 1, 2, 'embedded', 'system');

-- ================================================
-- 初始化菜单 - 部门管理
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'department', '部门管理', 'ApartmentOutlined', 'SystemDepartment', 'sys:dept:view', 50, 1, 1, 1, 'embedded', 'system');

SET @DEPT_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'department' AND type = 'menu');

-- 部门管理按钮权限
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @DEPT_MENU_ID, 'button', '新增部门', 'sys:department:create', 1, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @DEPT_MENU_ID, 'button', '编辑部门', 'sys:department:edit', 2, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @DEPT_MENU_ID, 'button', '删除部门', 'sys:department:delete', 3, 1, 1, 2, 'embedded', 'system');

-- ================================================
-- 初始化菜单 - 职位管理
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'position', '职位管理', 'IdcardOutlined', 'SystemPosition', 'sys:position:view', 60, 1, 1, 1, 'embedded', 'system');

SET @POSITION_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'position' AND type = 'menu');

-- 职位管理按钮权限
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @POSITION_MENU_ID, 'button', '新增职位', 'sys:position:create', 1, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @POSITION_MENU_ID, 'button', '编辑职位', 'sys:position:edit', 2, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @POSITION_MENU_ID, 'button', '删除职位', 'sys:position:delete', 3, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @POSITION_MENU_ID, 'button', '批量删除职位', 'sys:position:batchDelete', 4, 1, 1, 2, 'embedded', 'system');

-- ================================================
-- 初始化菜单 - Excel导出配置
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'excelExportConfig', '导出配置', 'FileExcelOutlined', 'SystemExcelExportConfig', 'sys:excel:exportConfig:view', 70, 1, 1, 1, 'embedded', 'system');

SET @EXCEL_EXPORT_CFG_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'excelExportConfig' AND type = 'menu');

-- Excel导出配置按钮权限
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '查看导出配置', 'sys:excel:exportConfig:list', 1, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '编辑导出配置', 'sys:excel:exportConfig:edit', 2, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '删除导出配置', 'sys:excel:exportConfig:delete', 3, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '导出登录日志', 'sys:excel:export:loginLog', 11, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '导出用户', 'sys:excel:export:user', 12, 1, 1, 2, 'embedded', 'system');

-- ================================================
-- 初始化菜单 - Excel导入配置
-- ================================================
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'excelImportConfig', '导入配置', 'FileExcelOutlined', 'SystemExcelImportConfig', 'sys:excel:importConfig:view', 80, 1, 1, 1, 'embedded', 'system');

SET @EXCEL_IMPORT_CFG_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'excelImportConfig' AND type = 'menu');

-- Excel导入配置按钮权限
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode, create_by) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_IMPORT_CFG_MENU_ID, 'button', '查看导入配置', 'sys:excel:importConfig:list', 1, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_IMPORT_CFG_MENU_ID, 'button', '编辑导入配置', 'sys:excel:importConfig:edit', 2, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_IMPORT_CFG_MENU_ID, 'button', '删除导入配置', 'sys:excel:importConfig:delete', 3, 1, 1, 2, 'embedded', 'system'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_IMPORT_CFG_MENU_ID, 'button', '下载导入模板', 'sys:excel:template:download', 10, 1, 1, 2, 'embedded', 'system');

-- ================================================
-- 角色菜单授权 - 管理员角色拥有所有权限
-- ================================================
INSERT INTO sys_role_menu (tenant_id, role_id, menu_id) 
SELECT @TENANT_ID, @ADMIN_ROLE_ID, id FROM sys_menu WHERE tenant_id = @TENANT_ID;

-- ================================================
-- 初始化系统配置
-- ================================================
INSERT INTO sys_config (config_key, config_value, tenant_id, create_by) VALUES 
('login.captcha', '{"mode":"none","image":{"keyPrefix":"captcha:image","expireSeconds":120,"width":120,"height":40,"length":4},"slider":{"secondaryEnabled":false,"keyPrefix":"captcha:slider","secondaryKeyPrefix":"captcha:secondary","tokenExpireSeconds":120,"provider":"redis-token"}}', @TENANT_ID, 'system');

-- ================================================
-- 租户隔离跳过配置
-- ================================================
INSERT INTO sys_tenant_ignore (scope, matcher, enabled, remark, create_by) VALUES 
('TABLE', 'sys_user', 1, '用户表不带租户字段', 'system'),
('TABLE', 'sys_tenant', 1, '租户表不带租户字段', 'system'),
('TABLE', 'sys_user_tenant', 1, '用户-租户关联表不带租户字段', 'system');

-- 完成提示
SELECT 'forgex_admin数据库初始数据插入完成！' AS message;
SELECT '默认管理员账号：admin' AS username;
SELECT '默认密码：123456' AS password;
