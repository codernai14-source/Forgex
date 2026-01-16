-- ================================================
-- 返回消息国际化配置表初始数据
-- ================================================

USE forgex_common;

-- ================================================
-- 全局异常消息 (common模块)
-- ================================================
INSERT INTO fx_i18n_message (module, prompt_code, text_i18n_json, enabled, version, create_by, create_time, update_time, deleted) VALUES
('common', 'NOT_LOGIN', '{"zh-CN":"未登录或登录过期","en-US":"Not logged in or login expired"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'NO_PERMISSION', '{"zh-CN":"无权限","en-US":"No permission"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'BAD_REQUEST', '{"zh-CN":"请求参数错误","en-US":"Bad request"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'DB_CONNECT_FAILED', '{"zh-CN":"数据库连接失败，请检查数据库服务与配置","en-US":"Database connection failed, please check database service and configuration"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'DATA_ACCESS_ERROR', '{"zh-CN":"数据访问异常","en-US":"Data access error"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'INTERNAL_SERVER_ERROR', '{"zh-CN":"服务器内部错误","en-US":"Internal server error"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'PARAM_EMPTY', '{"zh-CN":"参数不能为空","en-US":"Parameter cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'ID_EMPTY', '{"zh-CN":"ID不能为空","en-US":"ID cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'ID_INVALID', '{"zh-CN":"ID格式不正确","en-US":"Invalid ID format"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'NOT_FOUND', '{"zh-CN":"数据不存在","en-US":"Data not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'ALREADY_EXISTS', '{"zh-CN":"数据已存在","en-US":"Data already exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'OPERATION_FAILED', '{"zh-CN":"操作失败","en-US":"Operation failed"}', 1, 1, 'system', NOW(), NOW(), 0),
('common', 'OPERATION_SUCCESS', '{"zh-CN":"操作成功","en-US":"Operation successful"}', 1, 1, 'system', NOW(), NOW(), 0);

-- ================================================
-- 认证模块消息 (auth模块)
-- ================================================
INSERT INTO fx_i18n_message (module, prompt_code, text_i18n_json, enabled, version, create_by, create_time, update_time, deleted) VALUES
('auth', 'LANG_EMPTY', '{"zh-CN":"lang不能为空","en-US":"lang cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('auth', 'LANG_SET_FAILED', '{"zh-CN":"设置语言失败","en-US":"Failed to set language"}', 1, 1, 'system', NOW(), NOW(), 0),
('auth', 'NOT_LOGIN', '{"zh-CN":"未登录","en-US":"Not logged in"}', 1, 1, 'system', NOW(), NOW(), 0),
('auth', 'CAPTCHA_EMPTY', '{"zh-CN":"验证码不能为空","en-US":"Captcha cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('auth', 'CAPTCHA_INVALID', '{"zh-CN":"验证码不正确","en-US":"Invalid captcha"}', 1, 1, 'system', NOW(), NOW(), 0),
('auth', 'PUBLIC_KEY_NOT_CONFIGURED', '{"zh-CN":"未配置公钥","en-US":"Public key not configured"}', 1, 1, 'system', NOW(), NOW(), 0);

-- ================================================
-- 系统模块消息 (sys模块)
-- ================================================
INSERT INTO fx_i18n_message (module, prompt_code, text_i18n_json, enabled, version, create_by, create_time, update_time, deleted) VALUES
('sys', 'TENANT_ID_EMPTY', '{"zh-CN":"租户ID不能为空","en-US":"Tenant ID cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ADMIN_USER_NOT_FOUND', '{"zh-CN":"admin用户不存在","en-US":"Admin user not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'STATUS_EMPTY', '{"zh-CN":"状态不能为空","en-US":"Status cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'DICT_CODE_EMPTY', '{"zh-CN":"字典编码不能为空","en-US":"Dictionary code cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'NODE_PATH_EMPTY', '{"zh-CN":"nodePath不能为空","en-US":"Node path cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'DICT_ID_EMPTY', '{"zh-CN":"字典ID不能为空","en-US":"Dictionary ID cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'POSITION_NOT_FOUND', '{"zh-CN":"职位不存在","en-US":"Position not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'POSITION_CREATE_FAILED', '{"zh-CN":"新增职位失败","en-US":"Failed to create position"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'POSITION_UPDATE_FAILED', '{"zh-CN":"更新职位失败","en-US":"Failed to update position"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'POSITION_DELETE_FAILED', '{"zh-CN":"删除职位失败","en-US":"Failed to delete position"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'DEPARTMENT_NOT_FOUND', '{"zh-CN":"部门不存在","en-US":"Department not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'DEPARTMENT_CREATE_FAILED', '{"zh-CN":"新增部门失败","en-US":"Failed to create department"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'DEPARTMENT_UPDATE_FAILED', '{"zh-CN":"更新部门失败","en-US":"Failed to update department"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'DEPARTMENT_DELETE_FAILED', '{"zh-CN":"删除部门失败","en-US":"Failed to delete department"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'TENANT_NOT_FOUND', '{"zh-CN":"租户不存在","en-US":"Tenant not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MAIN_TENANT_NOT_FOUND', '{"zh-CN":"主租户不存在","en-US":"Main tenant not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'TENANT_CREATE_FAILED', '{"zh-CN":"新增租户失败","en-US":"Failed to create tenant"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'TENANT_UPDATE_FAILED', '{"zh-CN":"更新租户失败","en-US":"Failed to update tenant"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'TENANT_DELETE_FAILED', '{"zh-CN":"删除租户失败","en-US":"Failed to delete tenant"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'FILE_EMPTY', '{"zh-CN":"上传文件不能为空","en-US":"Upload file cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'FILE_UPLOAD_FAILED', '{"zh-CN":"文件上传失败: {0}","en-US":"File upload failed: {0}"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_NAME_EMPTY', '{"zh-CN":"角色名称不能为空","en-US":"Role name cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_CODE_EMPTY', '{"zh-CN":"角色编码不能为空","en-US":"Role code cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_CODE_EXISTS', '{"zh-CN":"角色编码已存在","en-US":"Role code already exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_NAME_EXISTS', '{"zh-CN":"角色名称已存在","en-US":"Role name already exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_KEY_EXISTS', '{"zh-CN":"角色键已存在","en-US":"Role key already exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_CODE_NOT_MODIFIABLE', '{"zh-CN":"角色编码不可修改","en-US":"Role code cannot be modified"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_CODE_USED_BY_OTHER', '{"zh-CN":"角色编码已被其他角色使用","en-US":"Role code already used by other role"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_NAME_USED_BY_OTHER', '{"zh-CN":"角色名称已被其他角色使用","en-US":"Role name already used by other role"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_KEY_USED_BY_OTHER', '{"zh-CN":"角色键已被其他角色使用","en-US":"Role key already used by other role"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_HAS_USERS', '{"zh-CN":"该角色下还有用户，无法删除","en-US":"This role has users, cannot be deleted"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MENU_ID_EMPTY', '{"zh-CN":"菜单ID不能为空","en-US":"Menu ID cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MENU_TYPE_INVALID', '{"zh-CN":"菜单类型不正确，只能是：catalog、menu、button","en-US":"Invalid menu type, must be: catalog, menu, button"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MENU_PATH_EMPTY', '{"zh-CN":"菜单路径不能为空","en-US":"Menu path cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'EXTERNAL_URL_EMPTY', '{"zh-CN":"外联URL不能为空","en-US":"External URL cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'EXTERNAL_URL_INVALID', '{"zh-CN":"外联URL格式不正确","en-US":"Invalid external URL format"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'PERM_KEY_EMPTY', '{"zh-CN":"按钮权限标识不能为空","en-US":"Button permission key cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'PERM_KEY_FORMAT_INVALID', '{"zh-CN":"权限标识格式不正确，必须符合 {module}:{entity}:{action} 格式，例如：sys:user:create","en-US":"Invalid permission key format, must follow {module}:{entity}:{action} format, e.g.: sys:user:create"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'PERM_KEY_EXISTS', '{"zh-CN":"权限标识已存在","en-US":"Permission key already exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'PERM_KEY_USED_BY_OTHER', '{"zh-CN":"权限标识已被其他菜单使用","en-US":"Permission key already used by other menu"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MENU_HAS_CHILDREN', '{"zh-CN":"该菜单下还有子菜单或按钮，无法删除","en-US":"This menu has sub-menus or buttons, cannot be deleted"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MENU_HAS_ROLE_AUTH', '{"zh-CN":"该菜单已被角色授权，无法删除","en-US":"This menu has been authorized to roles, cannot be deleted"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_ID_EMPTY', '{"zh-CN":"模块ID不能为空","en-US":"Module ID cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_CODE_EMPTY', '{"zh-CN":"模块编码不能为空","en-US":"Module code cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_NAME_EMPTY', '{"zh-CN":"模块名称不能为空","en-US":"Module name cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_CODE_EXISTS', '{"zh-CN":"模块编码已存在","en-US":"Module code already exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_NAME_EXISTS', '{"zh-CN":"模块名称已存在","en-US":"Module name already exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_CODE_INVALID', '{"zh-CN":"模块编码格式不正确，只能包含字母、数字、下划线，长度2-50","en-US":"Invalid module code format, can only contain letters, numbers, underscores, length 2-50"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_CODE_USED_BY_OTHER', '{"zh-CN":"模块编码已被其他模块使用","en-US":"Module code already used by other module"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_NAME_USED_BY_OTHER', '{"zh-CN":"模块名称已被其他模块使用","en-US":"Module name already used by other module"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_HAS_MENUS', '{"zh-CN":"该模块下还有菜单，无法删除","en-US":"This module has menus, cannot be deleted"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'MODULE_HAS_ROLE_AUTH', '{"zh-CN":"该模块的菜单已被角色授权，无法删除","en-US":"Menus of this module have been authorized to roles, cannot be deleted"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'USER_ACCOUNT_EMPTY', '{"zh-CN":"账号不能为空","en-US":"Account cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'USER_NAME_EMPTY', '{"zh-CN":"用户名不能为空","en-US":"Username cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'USER_ACCOUNT_EXISTS', '{"zh-CN":"账号已存在","en-US":"Account already exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'EMAIL_INVALID', '{"zh-CN":"邮箱格式不正确","en-US":"Invalid email format"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'PHONE_INVALID', '{"zh-CN":"手机号格式不正确","en-US":"Invalid phone format"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'USER_NOT_FOUND', '{"zh-CN":"用户不存在","en-US":"User not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'USER_ACCOUNT_USED_BY_OTHER', '{"zh-CN":"账号已被其他用户使用","en-US":"Account already used by other user"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'USER_NOT_BOUND_TO_TENANT', '{"zh-CN":"用户未绑定该租户，无法分配角色","en-US":"User not bound to this tenant, cannot assign roles"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'ROLE_ID_INVALID', '{"zh-CN":"角色ID格式不正确","en-US":"Invalid role ID format"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'INVALID_ROLE_OR_CROSS_TENANT', '{"zh-CN":"存在无效角色或跨租户角色","en-US":"Invalid role or cross-tenant role exists"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'PARENT_NODE_NOT_FOUND', '{"zh-CN":"父节点不存在","en-US":"Parent node not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'DICT_NOT_FOUND', '{"zh-CN":"字典不存在","en-US":"Dictionary not found"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'DICT_HAS_CHILDREN', '{"zh-CN":"该字典下存在子节点，无法删除","en-US":"This dictionary has child nodes, cannot be deleted"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'TABLE_CODE_EMPTY', '{"zh-CN":"tableCode不能为空","en-US":"tableCode cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0),
('sys', 'USER_ACCOUNT_EMPTY', '{"zh-CN":"用户账号不能为空","en-US":"User account cannot be empty"}', 1, 1, 'system', NOW(), NOW(), 0);

SELECT '返回消息国际化配置表初始数据插入完成！' AS message;
