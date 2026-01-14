CREATE DATABASE IF NOT EXISTS forgex_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS forgex_common DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS forgex_history DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS forgex_job DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS forgex_workflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS forgex_scada DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE forgex_admin;

-- 租户表
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '租户ID',
    tenant_name VARCHAR(100) NOT NULL COMMENT '租户名称',
    tenant_code VARCHAR(100) UNIQUE COMMENT '租户编码',
    description VARCHAR(255) COMMENT '描述',
    logo TEXT COMMENT 'Logo(Base64/URL)',
    tenant_type VARCHAR(20) DEFAULT 'MAIN_TENANT' COMMENT '租户类别：MAIN_TENANT-主租户，CUSTOMER_TENANT-客户租户，SUPPLIER_TENANT-供应商租户',
    status TINYINT DEFAULT 1 COMMENT '状态 (1:启用, 0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 (0:未删除, 1:已删除)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    account VARCHAR(50) NOT NULL UNIQUE COMMENT '账号',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码 (加密)',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态 (1:启用, 0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户附属信息表
CREATE TABLE IF NOT EXISTS sys_user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    political_status VARCHAR(50) NULL COMMENT '政治面貌',
    education VARCHAR(50) NULL COMMENT '学历',
    birth_place VARCHAR(255) NULL COMMENT '籍贯',
    intro VARCHAR(512) NULL COMMENT '个人简介',
    home_address VARCHAR(255) NULL COMMENT '家庭住址',
    emergency_contact VARCHAR(50) NULL COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) NULL COMMENT '紧急联系人电话',
    referrer VARCHAR(50) NULL COMMENT '引荐人',
    work_history JSON NULL COMMENT '工作经历(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_profile_user_tenant (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户附属信息表';

CREATE TABLE IF NOT EXISTS sys_user_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    pref_order INT DEFAULT 0 COMMENT '喜好排序（越大越靠前）',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认租户：1默认 0非默认',
    last_used DATETIME NULL COMMENT '最后使用时间',
    UNIQUE KEY uk_user_tenant (user_id, tenant_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id) REFERENCES sys_tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户租户关联表';

-- 角色表（保留tenant_id实现数据隔离）
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识',
    status TINYINT DEFAULT 1 COMMENT '状态',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_key VARCHAR(100) NOT NULL UNIQUE COMMENT '权限标识',
    url VARCHAR(255) COMMENT 'URL',
    method VARCHAR(10) COMMENT '方法 (GET/POST等)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值 (JSON)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
INSERT INTO sys_config (config_key, config_value, tenant_id) VALUES (
  'login.captcha',
  '{"mode":"none","image":{"keyPrefix":"captcha:image","expireSeconds":120,"width":120,"height":40,"length":4},"slider":{"secondaryEnabled":false,"keyPrefix":"captcha:slider","secondaryKeyPrefix":"captcha:secondary","tokenExpireSeconds":120,"provider":"redis-token"}}',
  1
);

-- 定时任务表
CREATE TABLE IF NOT EXISTS sys_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    cron_expression VARCHAR(255) NOT NULL COMMENT 'Cron表达式',
    status TINYINT DEFAULT 1 COMMENT '状态',
    invoke_target VARCHAR(255) NOT NULL COMMENT '调用目标 (方法路径)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 初始数据
INSERT INTO sys_tenant (tenant_name, tenant_code, description, tenant_type, status) VALUES ('default', 'default', '默认租户', 'MAIN_TENANT', 1);
INSERT INTO sys_tenant (tenant_name, tenant_code, description, tenant_type, status) VALUES ('tenant2', 'tenant2', '第二个租户', 'CUSTOMER_TENANT', 1);

INSERT INTO sys_user (account, username, password, email, status) VALUES ('admin', 'admin', '$2a$10$7EqJtq98hPqEX7fNLEyguOg03fZ7p.XN3okEV0H1Bs8Q5q8K9bL2u', 'admin@forgex.com', 1);

INSERT INTO sys_user_tenant (user_id, tenant_id) VALUES (1, 1);
INSERT INTO sys_user_tenant (user_id, tenant_id) VALUES (1, 2);

ALTER TABLE sys_user ADD INDEX idx_username (username);
ALTER TABLE sys_user ADD INDEX idx_account (account);
ALTER TABLE sys_role ADD INDEX idx_role_key (role_key);
ALTER TABLE sys_user_tenant ADD INDEX idx_user_pref (user_id, pref_order);

-- 租户隔离跳过配置表
CREATE TABLE IF NOT EXISTS sys_tenant_ignore (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    scope VARCHAR(20) NOT NULL COMMENT '作用域：TABLE/SERVICE/MAPPER',
    matcher VARCHAR(255) NOT NULL COMMENT '匹配内容：表名/全限定类名/全限定类名#方法名',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户隔离跳过配置表';

-- 初始化：忽略无租户字段的表
INSERT INTO sys_tenant_ignore(scope, matcher, enabled, remark) VALUES ('TABLE', 'sys_user', 1, '用户表不带租户字段');
INSERT INTO sys_tenant_ignore(scope, matcher, enabled, remark) VALUES ('TABLE', 'sys_tenant', 1, '租户表不带租户字段');
INSERT INTO sys_tenant_ignore(scope, matcher, enabled, remark) VALUES ('TABLE', 'sys_user_tenant', 1, '用户-租户关联表不带租户字段');

-- 绑定表增加租户字段，支持按租户隔离角色绑定
ALTER TABLE sys_user_role ADD COLUMN tenant_id BIGINT;
ALTER TABLE sys_user_role ADD INDEX idx_user_role_tenant (tenant_id, user_id, role_id);

-- 模块表
CREATE TABLE IF NOT EXISTS sys_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    order_num INT DEFAULT 0,
    visible TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1,
    UNIQUE KEY uk_module_code_tenant (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模块表';

-- 菜单表（含目录/菜单/按钮）
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    type VARCHAR(20) NOT NULL COMMENT 'dir/menu/button',
    path VARCHAR(255) COMMENT '路由路径',
    name VARCHAR(100) NOT NULL COMMENT '显示名称',
    icon VARCHAR(50),
    component_key VARCHAR(100) COMMENT '前端组件键',
    perm_key VARCHAR(100) COMMENT '按钮权限键',
    order_num INT DEFAULT 0,
    visible TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1,
    INDEX idx_menu_module (module_id),
    INDEX idx_menu_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单授权
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    INDEX idx_role_menu (tenant_id, role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单授权';

-- 默认模块与菜单（租户1）
INSERT INTO sys_module (tenant_id, code, name, icon, order_num, visible, status) VALUES (1, 'sys', '系统', 'setting', 10, 1, 1);
SET @SYS_ID = (SELECT id FROM sys_module WHERE tenant_id=1 AND code='sys');
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, order_num, visible, status) VALUES
 (1, @SYS_ID, 0, 'menu', 'user', '用户管理', 'user', 'SysUser', 10, 1, 1),
 (1, @SYS_ID, 0, 'menu', 'role', '角色管理', 'team', 'SysRole', 20, 1, 1);
SET @USER_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id=1 AND name='用户管理' AND type='menu' LIMIT 1);
SET @ROLE_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id=1 AND name='角色管理' AND type='menu' LIMIT 1);
INSERT INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status) VALUES
 (1, @SYS_ID, @USER_MENU_ID, 'button', '用户查看', 'user.view', 1, 1, 1),
 (1, @SYS_ID, @USER_MENU_ID, 'button', '用户编辑', 'user.edit', 2, 1, 1),
 (1, @SYS_ID, @ROLE_MENU_ID, 'button', '角色查看', 'role.view', 1, 1, 1),
 (1, @SYS_ID, @ROLE_MENU_ID, 'button', '角色编辑', 'role.edit', 2, 1, 1);

-- 管理员角色授权所有菜单（租户1）
SET @ADMIN_ROLE_ID = (SELECT id FROM sys_role WHERE tenant_id=1 AND role_key='admin' LIMIT 1);
INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
 SELECT 1, @ADMIN_ROLE_ID, id FROM sys_menu WHERE tenant_id=1;

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NULL COMMENT '用户ID',
    account VARCHAR(50) NULL COMMENT '登录账号',
    tenant_id BIGINT NULL COMMENT '租户ID',
    login_ip VARCHAR(64) NULL COMMENT '登录IP',
    login_region VARCHAR(255) NULL COMMENT 'IP归属地',
    user_agent VARCHAR(512) NULL COMMENT '浏览器UA',
    login_time DATETIME NULL COMMENT '登录时间',
    logout_time DATETIME NULL COMMENT '登出时间',
    token_value VARCHAR(512) NULL COMMENT 'tokenValue',
    logout_reason VARCHAR(50) NULL COMMENT '登出原因',
    status TINYINT DEFAULT 1 COMMENT '状态：1成功 0失败',
    reason VARCHAR(255) NULL COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_login_tenant_time (tenant_id, login_time),
    INDEX idx_login_user_tenant (user_id, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- 补齐 sys_user 最后登录信息字段（若缺失可手工执行）
ALTER TABLE sys_user ADD COLUMN last_login_ip VARCHAR(64) NULL COMMENT '最后登录IP';
ALTER TABLE sys_user ADD COLUMN last_login_region VARCHAR(255) NULL COMMENT '最后登录地区';
ALTER TABLE sys_user ADD COLUMN last_login_time DATETIME NULL COMMENT '最后登录时间';

-- ================================
-- 切换至 forgex_common 库，创建用户页面样式配置表
-- ================================
USE forgex_common;

CREATE TABLE IF NOT EXISTS sys_user_style_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键，例如：layout.style',
    config_json TEXT COMMENT '配置内容JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_user_tenant_key (user_id, tenant_id, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户页面样式配置表';

-- ================================
-- Excel 导入导出配置表（forgex_common）
-- ================================

CREATE TABLE IF NOT EXISTS fx_excel_export_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    table_name VARCHAR(255) NOT NULL COMMENT '表名',
    table_code VARCHAR(100) NOT NULL COMMENT '表编号',
    header_style_json TEXT NULL COMMENT '表头样式(JSON)',
    title VARCHAR(255) NULL COMMENT '标题列名称',
    subtitle VARCHAR(255) NULL COMMENT '标题列小字说明',
    export_format VARCHAR(20) NOT NULL DEFAULT 'xlsx' COMMENT '导出格式(xlsx/csv)',
    enable_total TINYINT NOT NULL DEFAULT 0 COMMENT '是否开启总计',
    version INT NOT NULL DEFAULT 1 COMMENT '版本',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_export_tenant_code (tenant_id, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Excel导出配置主表';

CREATE TABLE IF NOT EXISTS fx_excel_export_config_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    config_id BIGINT NOT NULL COMMENT '主表ID',
    export_field VARCHAR(128) NOT NULL COMMENT '导出字段',
    field_name VARCHAR(255) NULL COMMENT '字段名(默认列头)',
    i18n_json TEXT NULL COMMENT '字段多语言配置(JSON)',
    header_style_json TEXT NULL COMMENT '列头样式(JSON)',
    cell_style_json TEXT NULL COMMENT '列内容样式(JSON)',
    order_num INT NOT NULL DEFAULT 0 COMMENT '顺序',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_export_cfg (tenant_id, config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Excel导出配置子表';

CREATE TABLE IF NOT EXISTS fx_excel_import_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    table_name VARCHAR(255) NOT NULL COMMENT '表名',
    table_code VARCHAR(100) NOT NULL COMMENT '表编号',
    title VARCHAR(255) NULL COMMENT '标题列名称',
    subtitle VARCHAR(255) NULL COMMENT '标题列小字说明',
    version INT NOT NULL DEFAULT 1 COMMENT '版本',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    UNIQUE KEY uk_import_tenant_code (tenant_id, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Excel导入配置主表';

CREATE TABLE IF NOT EXISTS fx_excel_import_config_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    config_id BIGINT NOT NULL COMMENT '主表ID',
    i18n_json TEXT NULL COMMENT '字段多语言配置(JSON)',
    import_field VARCHAR(128) NOT NULL COMMENT '导入字段',
    field_type VARCHAR(50) NOT NULL DEFAULT 'string' COMMENT '字段类型(time/date/datetime/dict/...)',
    dict_code VARCHAR(100) NULL COMMENT '字典编号',
    required TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填',
    order_num INT NOT NULL DEFAULT 0 COMMENT '顺序',
    create_by VARCHAR(64) NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) NULL COMMENT '修改人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_import_cfg (tenant_id, config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Excel导入配置子表';

-- 默认导出/导入配置（租户1）
INSERT IGNORE INTO fx_excel_export_config (tenant_id, table_name, table_code, title, subtitle, export_format, enable_total, version)
VALUES
(1, '用户导出', 'sys_user', '用户导出', '系统管理-用户管理', 'xlsx', 0, 1),
(1, '登录日志导出', 'sys_login_log', '登录日志导出', '系统管理-登录日志', 'xlsx', 0, 1);

SET @EXPORT_USER_CFG_ID = (SELECT id FROM fx_excel_export_config WHERE tenant_id=1 AND table_code='sys_user' LIMIT 1);
SET @EXPORT_LOGINLOG_CFG_ID = (SELECT id FROM fx_excel_export_config WHERE tenant_id=1 AND table_code='sys_login_log' LIMIT 1);

INSERT IGNORE INTO fx_excel_export_config_item (tenant_id, config_id, export_field, field_name, order_num)
VALUES
(1, @EXPORT_USER_CFG_ID, 'id', '用户ID', 1),
(1, @EXPORT_USER_CFG_ID, 'account', '账号', 2),
(1, @EXPORT_USER_CFG_ID, 'username', '用户名', 3),
(1, @EXPORT_USER_CFG_ID, 'phone', '手机号', 4),
(1, @EXPORT_USER_CFG_ID, 'email', '邮箱', 5),
(1, @EXPORT_USER_CFG_ID, 'status', '状态', 6),
(1, @EXPORT_USER_CFG_ID, 'lastLoginTime', '最后登录时间', 7),
(1, @EXPORT_USER_CFG_ID, 'lastLoginIp', '最后登录IP', 8),
(1, @EXPORT_USER_CFG_ID, 'lastLoginRegion', '最后登录地区', 9);

INSERT IGNORE INTO fx_excel_export_config_item (tenant_id, config_id, export_field, field_name, order_num)
VALUES
(1, @EXPORT_LOGINLOG_CFG_ID, 'account', '账号', 1),
(1, @EXPORT_LOGINLOG_CFG_ID, 'loginIp', '登录IP', 2),
(1, @EXPORT_LOGINLOG_CFG_ID, 'loginRegion', '归属地', 3),
(1, @EXPORT_LOGINLOG_CFG_ID, 'userAgent', '浏览器UA', 4),
(1, @EXPORT_LOGINLOG_CFG_ID, 'loginTime', '登录时间', 5),
(1, @EXPORT_LOGINLOG_CFG_ID, 'logoutTime', '登出时间', 6),
(1, @EXPORT_LOGINLOG_CFG_ID, 'logoutReason', '登出原因', 7),
(1, @EXPORT_LOGINLOG_CFG_ID, 'status', '状态', 8),
(1, @EXPORT_LOGINLOG_CFG_ID, 'reason', '失败原因', 9);

INSERT IGNORE INTO fx_excel_import_config (tenant_id, table_name, table_code, title, subtitle, version)
VALUES
(1, '用户导入模板', 'sys_user', '用户导入模板', '系统管理-用户管理', 1);

SET @IMPORT_USER_CFG_ID = (SELECT id FROM fx_excel_import_config WHERE tenant_id=1 AND table_code='sys_user' LIMIT 1);

INSERT IGNORE INTO fx_excel_import_config_item (tenant_id, config_id, import_field, field_type, dict_code, required, order_num)
VALUES
(1, @IMPORT_USER_CFG_ID, 'account', 'string', NULL, 1, 1),
(1, @IMPORT_USER_CFG_ID, 'username', 'string', NULL, 1, 2),
(1, @IMPORT_USER_CFG_ID, 'phone', 'string', NULL, 0, 3),
(1, @IMPORT_USER_CFG_ID, 'email', 'string', NULL, 0, 4);
