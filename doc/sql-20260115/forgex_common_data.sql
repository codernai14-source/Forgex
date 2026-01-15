-- ================================================
-- Forgex Common 数据库初始数据
-- 数据库: forgex_common
-- 说明: 公共配置和通用功能初始数据
-- ================================================

USE forgex_common;

-- ================================================
-- 初始化用户页面样式配置
-- ================================================
-- 注意：这里需要先从forgex_admin数据库获取管理员用户ID和租户ID
-- 如果跨库查询不可用，请手动替换以下变量值
SET @ADMIN_USER_ID = 1;
SET @TENANT_ID = 1;

INSERT INTO sys_user_style_config (user_id, tenant_id, config_key, config_json) VALUES
(@ADMIN_USER_ID, @TENANT_ID, 'layout.style', '{"theme":"default","layout":"side","navTheme":"dark","fixedHeader":true,"autoHideHeader":false,"fixSiderbar":true}');

-- ================================================
-- 初始化Excel导出配置
-- ================================================
INSERT INTO fx_excel_export_config (tenant_id, table_name, table_code, title, subtitle, export_format, enable_total, version, create_by) VALUES
(@TENANT_ID, '用户导出', 'sys_user', '用户导出', '系统管理-用户管理', 'xlsx', 0, 1, 'system'),
(@TENANT_ID, '登录日志导出', 'sys_login_log', '登录日志导出', '系统管理-登录日志', 'xlsx', 0, 1, 'system');

SET @EXPORT_USER_CFG_ID = (SELECT id FROM fx_excel_export_config WHERE tenant_id=@TENANT_ID AND table_code='sys_user' LIMIT 1);
SET @EXPORT_LOGINLOG_CFG_ID = (SELECT id FROM fx_excel_export_config WHERE tenant_id=@TENANT_ID AND table_code='sys_login_log' LIMIT 1);

-- ================================================
-- 初始化Excel导出配置项 - 用户导出
-- ================================================
INSERT INTO fx_excel_export_config_item (tenant_id, config_id, export_field, field_name, order_num, create_by) VALUES
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'id', '用户ID', 1, 'system'),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'account', '账号', 2, 'system'),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'username', '用户名', 3, 'system'),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'phone', '手机号', 4, 'system'),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'email', '邮箱', 5, 'system'),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'status', '状态', 6, 'system'),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'lastLoginTime', '最后登录时间', 7, 'system'),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'lastLoginIp', '最后登录IP', 8, 'system'),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'lastLoginRegion', '最后登录地区', 9, 'system');

-- ================================================
-- 初始化Excel导出配置项 - 登录日志导出
-- ================================================
INSERT INTO fx_excel_export_config_item (tenant_id, config_id, export_field, field_name, order_num, create_by) VALUES
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'account', '账号', 1, 'system'),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'loginIp', '登录IP', 2, 'system'),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'loginRegion', '归属地', 3, 'system'),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'userAgent', '浏览器UA', 4, 'system'),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'loginTime', '登录时间', 5, 'system'),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'logoutTime', '登出时间', 6, 'system'),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'logoutReason', '登出原因', 7, 'system'),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'status', '状态', 8, 'system'),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'reason', '失败原因', 9, 'system');

-- ================================================
-- 初始化Excel导入配置
-- ================================================
INSERT INTO fx_excel_import_config (tenant_id, table_name, table_code, title, subtitle, version, create_by) VALUES
(@TENANT_ID, '用户导入模板', 'sys_user', '用户导入模板', '系统管理-用户管理', 1, 'system');

SET @IMPORT_USER_CFG_ID = (SELECT id FROM fx_excel_import_config WHERE tenant_id=@TENANT_ID AND table_code='sys_user' LIMIT 1);

-- ================================================
-- 初始化Excel导入配置项 - 用户导入
-- ================================================
INSERT INTO fx_excel_import_config_item (tenant_id, config_id, import_field, field_type, dict_code, required, order_num, create_by) VALUES
(@TENANT_ID, @IMPORT_USER_CFG_ID, 'account', 'string', NULL, 1, 1, 'system'),
(@TENANT_ID, @IMPORT_USER_CFG_ID, 'username', 'string', NULL, 1, 2, 'system'),
(@TENANT_ID, @IMPORT_USER_CFG_ID, 'phone', 'string', NULL, 0, 3, 'system'),
(@TENANT_ID, @IMPORT_USER_CFG_ID, 'email', 'string', NULL, 0, 4, 'system');

-- 完成提示
SELECT 'forgex_common数据库初始数据插入完成！' AS message;
