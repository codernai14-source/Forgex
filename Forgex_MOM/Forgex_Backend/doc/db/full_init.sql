-- 创建数据库
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
    status TINYINT DEFAULT 1 COMMENT '状态 (1:启用, 0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 (0:未删除, 1:已删除)',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 用户表
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
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
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
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    UNIQUE KEY uk_profile_user_tenant (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户附属信息表';

-- 用户租户关联表
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

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
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
    deleted TINYINT DEFAULT 0,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    INDEX idx_user_role_tenant (tenant_id, user_id, role_id)
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
    deleted TINYINT DEFAULT 0,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

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
    deleted TINYINT DEFAULT 0,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 租户隔离跳过配置表
CREATE TABLE IF NOT EXISTS sys_tenant_ignore (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    scope VARCHAR(20) NOT NULL COMMENT '作用域：TABLE/SERVICE/MAPPER',
    matcher VARCHAR(255) NOT NULL COMMENT '匹配内容：表名/全限定类名/全限定类名#方法名',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户隔离跳过配置表';

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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    menu_level INT COMMENT '菜单级别',
    menu_mode VARCHAR(20) COMMENT '菜单模式',
    external_url VARCHAR(255) COMMENT '外部链接',
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

-- 部门表
CREATE TABLE IF NOT EXISTS sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    code VARCHAR(50),
    leader VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    order_num INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人',
    INDEX idx_dept_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 职位表
CREATE TABLE IF NOT EXISTS sys_position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    description VARCHAR(255),
    order_num INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

-- 初始化数据
-- 使用存储过程安全地添加缺失字段（兼容MySQL 5.7）
DELIMITER $$

-- 检查并添加字段的存储过程
CREATE PROCEDURE add_column_if_not_exists(IN table_name VARCHAR(255), IN column_name VARCHAR(255), IN column_definition VARCHAR(500))
BEGIN
    DECLARE column_count INT;
    
    SELECT COUNT(*) INTO column_count 
    FROM information_schema.columns 
    WHERE table_schema = DATABASE() 
    AND table_name = table_name 
    AND column_name = column_name;
    
    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' ADD COLUMN ', column_name, ' ', column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- 调用存储过程添加缺失字段
CALL add_column_if_not_exists('sys_tenant', 'description', 'VARCHAR(255) COMMENT ''描述''');
CALL add_column_if_not_exists('sys_role', 'description', 'VARCHAR(500) COMMENT ''描述''');

CALL add_column_if_not_exists('sys_menu', 'component_key', 'VARCHAR(100) COMMENT ''前端组件键''');
CALL add_column_if_not_exists('sys_menu', 'perm_key', 'VARCHAR(100) COMMENT ''按钮权限键''');
CALL add_column_if_not_exists('sys_menu', 'menu_level', 'INT COMMENT ''菜单级别''');
CALL add_column_if_not_exists('sys_menu', 'menu_mode', 'VARCHAR(20) COMMENT ''菜单模式''');
CALL add_column_if_not_exists('sys_menu', 'external_url', 'VARCHAR(255) COMMENT ''外部链接''');

CALL add_column_if_not_exists('sys_module', 'icon', 'VARCHAR(50) COMMENT ''图标''');
CALL add_column_if_not_exists('sys_module', 'order_num', 'INT DEFAULT 0 COMMENT ''排序''');
CALL add_column_if_not_exists('sys_module', 'visible', 'TINYINT DEFAULT 1 COMMENT ''是否可见''');
CALL add_column_if_not_exists('sys_module', 'status', 'TINYINT DEFAULT 1 COMMENT ''状态''');

CALL add_column_if_not_exists('sys_user', 'email', 'VARCHAR(100) COMMENT ''邮箱''');
CALL add_column_if_not_exists('sys_user', 'phone', 'VARCHAR(20) COMMENT ''手机号''');

CALL add_column_if_not_exists('sys_user_tenant', 'is_default', 'TINYINT DEFAULT 0 COMMENT ''是否默认租户''');
CALL add_column_if_not_exists('sys_user_tenant', 'pref_order', 'INT DEFAULT 0 COMMENT ''喜好排序''');
CALL add_column_if_not_exists('sys_user_tenant', 'last_used', 'DATETIME COMMENT ''最后使用时间''');

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 1. 插入租户（如果不存在）
INSERT IGNORE INTO sys_tenant (tenant_name, tenant_code, description, status) VALUES ('默认租户', 'default', '默认租户', 1);
SET @TENANT_ID = (SELECT id FROM sys_tenant WHERE tenant_code = 'default');

-- 2. 插入管理员用户 (密码: 123456，如果不存在)
INSERT IGNORE INTO sys_user (account, username, password, email, status) VALUES ('admin', 'admin', '$2a$10$7EqJtq98hPqEX7fNLEyguOg03fZ7p.XN3okEV0H1Bs8Q5q8K9bL2u', 'admin@forgex.com', 1);
SET @ADMIN_USER_ID = (SELECT id FROM sys_user WHERE account = 'admin');

-- 3. 用户租户关联（如果不存在）
INSERT IGNORE INTO sys_user_tenant (user_id, tenant_id, is_default, pref_order) VALUES (@ADMIN_USER_ID, @TENANT_ID, 1, 100);

-- 4. 插入角色（如果不存在）
INSERT IGNORE INTO sys_role (role_name, role_key, description, status, tenant_id) VALUES ('系统管理员', 'admin', '系统管理员', 1, @TENANT_ID);
SET @ADMIN_ROLE_ID = (SELECT id FROM sys_role WHERE role_key = 'admin' AND tenant_id = @TENANT_ID);

INSERT IGNORE INTO sys_role (role_name, role_key, description, status, tenant_id) VALUES ('普通用户', 'user', '普通用户', 1, @TENANT_ID);
SET @USER_ROLE_ID = (SELECT id FROM sys_role WHERE role_key = 'user' AND tenant_id = @TENANT_ID);

INSERT IGNORE INTO sys_role (role_name, role_key, description, status, tenant_id) VALUES ('部门经理', 'manager', '部门经理', 1, @TENANT_ID);
SET @MANAGER_ROLE_ID = (SELECT id FROM sys_role WHERE role_key = 'manager' AND tenant_id = @TENANT_ID);

INSERT IGNORE INTO sys_role (role_name, role_key, description, status, tenant_id) VALUES ('系统审计员', 'auditor', '系统审计员', 1, @TENANT_ID);
SET @AUDITOR_ROLE_ID = (SELECT id FROM sys_role WHERE role_key = 'auditor' AND tenant_id = @TENANT_ID);

-- 5. 用户角色关联（如果不存在）
INSERT IGNORE INTO sys_user_role (user_id, role_id, tenant_id) VALUES (@ADMIN_USER_ID, @ADMIN_ROLE_ID, @TENANT_ID);

-- 6. 插入模块（如果不存在）
INSERT IGNORE INTO sys_module (tenant_id, code, name, icon, order_num, visible, status) VALUES (@TENANT_ID, 'sys', '系统管理', 'SettingOutlined', 10, 1, 1);
SET @SYS_MODULE_ID = (SELECT id FROM sys_module WHERE code = 'sys' AND tenant_id = @TENANT_ID);

-- 7. 插入菜单（如果不存在）
-- 系统管理主页
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'dashboard', '系统管理主页', 'DashboardOutlined', 'SystemDashboard', 'sys:dashboard:view', 1, 1, 1, 1, 'embedded');
SET @DASHBOARD_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'dashboard' AND type = 'menu');

-- 用户管理菜单
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'user', '用户管理', 'UserOutlined', 'SystemUser', 'sys:user:view', 10, 1, 1, 1, 'embedded');
SET @USER_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'user' AND type = 'menu');

-- 角色管理菜单
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'role', '角色管理', 'TeamOutlined', 'SystemRole', 'sys:role:view', 20, 1, 1, 1, 'embedded');
SET @ROLE_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'role' AND type = 'menu');

-- 模块管理菜单
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'module', '模块管理', 'AppstoreOutlined', 'SystemModule', 'sys:module:view', 30, 1, 1, 1, 'embedded');
SET @MODULE_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'module' AND type = 'menu');

-- 菜单管理菜单
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'menu', '菜单管理', 'MenuOutlined', 'SystemMenu', 'sys:menu:view', 40, 1, 1, 1, 'embedded');
SET @MENU_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'menu' AND type = 'menu');

-- 部门管理菜单
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'department', '部门管理', 'ApartmentOutlined', 'SystemDepartment', 'sys:dept:view', 50, 1, 1, 1, 'embedded');
SET @DEPT_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'department' AND type = 'menu');

-- 职位管理菜单
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'position', '职位管理', 'IdcardOutlined', 'SystemPosition', 'sys:position:view', 60, 1, 1, 1, 'embedded');
SET @POSITION_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'position' AND type = 'menu');

-- 用户管理按钮权限
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '新增用户', 'sys:user:create', 1, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '编辑用户', 'sys:user:edit', 2, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '删除用户', 'sys:user:delete', 3, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '批量删除用户', 'sys:user:batchDelete', 4, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '重置密码', 'sys:user:resetPwd', 5, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '导出用户', 'sys:user:export', 6, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @USER_MENU_ID, 'button', '分配角色', 'sys:user:assignRole', 7, 1, 1, 2, 'embedded');

-- 角色管理按钮权限
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '新增角色', 'sys:role:create', 1, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '编辑角色', 'sys:role:edit', 2, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '删除角色', 'sys:role:delete', 3, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '批量删除角色', 'sys:role:batchDelete', 4, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @ROLE_MENU_ID, 'button', '菜单授权', 'sys:role:authMenu', 5, 1, 1, 2, 'embedded');

-- 模块管理按钮权限
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @MODULE_MENU_ID, 'button', '新增模块', 'sys:module:create', 1, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @MODULE_MENU_ID, 'button', '编辑模块', 'sys:module:edit', 2, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @MODULE_MENU_ID, 'button', '删除模块', 'sys:module:delete', 3, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @MODULE_MENU_ID, 'button', '批量删除模块', 'sys:module:batchDelete', 4, 1, 1, 2, 'embedded');

-- 菜单管理按钮权限
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @MENU_MENU_ID, 'button', '新增菜单', 'sys:menu:create', 1, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @MENU_MENU_ID, 'button', '编辑菜单', 'sys:menu:edit', 2, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @MENU_MENU_ID, 'button', '删除菜单', 'sys:menu:delete', 3, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @MENU_MENU_ID, 'button', '批量删除菜单', 'sys:menu:batchDelete', 4, 1, 1, 2, 'embedded');

-- 部门管理按钮权限
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @DEPT_MENU_ID, 'button', '新增部门', 'sys:department:create', 1, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @DEPT_MENU_ID, 'button', '编辑部门', 'sys:department:edit', 2, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @DEPT_MENU_ID, 'button', '删除部门', 'sys:department:delete', 3, 1, 1, 2, 'embedded');

-- 职位管理按钮权限
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @POSITION_MENU_ID, 'button', '新增职位', 'sys:position:create', 1, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @POSITION_MENU_ID, 'button', '编辑职位', 'sys:position:edit', 2, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @POSITION_MENU_ID, 'button', '删除职位', 'sys:position:delete', 3, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @POSITION_MENU_ID, 'button', '批量删除职位', 'sys:position:batchDelete', 4, 1, 1, 2, 'embedded');

-- Excel 配置菜单
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'excelExportConfig', '导出配置', 'FileExcelOutlined', 'SystemExcelExportConfig', 'sys:excel:exportConfig:view', 70, 1, 1, 1, 'embedded');
SET @EXCEL_EXPORT_CFG_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'excelExportConfig' AND type = 'menu');

INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, path, name, icon, component_key, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, 0, 'menu', 'excelImportConfig', '导入配置', 'FileExcelOutlined', 'SystemExcelImportConfig', 'sys:excel:importConfig:view', 80, 1, 1, 1, 'embedded');
SET @EXCEL_IMPORT_CFG_MENU_ID = (SELECT id FROM sys_menu WHERE tenant_id = @TENANT_ID AND path = 'excelImportConfig' AND type = 'menu');

-- Excel 配置按钮权限
INSERT IGNORE INTO sys_menu (tenant_id, module_id, parent_id, type, name, perm_key, order_num, visible, status, menu_level, menu_mode) VALUES
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '查看导出配置', 'sys:excel:exportConfig:list', 1, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '编辑导出配置', 'sys:excel:exportConfig:edit', 2, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '删除导出配置', 'sys:excel:exportConfig:delete', 3, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_IMPORT_CFG_MENU_ID, 'button', '查看导入配置', 'sys:excel:importConfig:list', 1, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_IMPORT_CFG_MENU_ID, 'button', '编辑导入配置', 'sys:excel:importConfig:edit', 2, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_IMPORT_CFG_MENU_ID, 'button', '删除导入配置', 'sys:excel:importConfig:delete', 3, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_IMPORT_CFG_MENU_ID, 'button', '下载导入模板', 'sys:excel:template:download', 10, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '导出登录日志', 'sys:excel:export:loginLog', 11, 1, 1, 2, 'embedded'),
(@TENANT_ID, @SYS_MODULE_ID, @EXCEL_EXPORT_CFG_MENU_ID, 'button', '导出用户', 'sys:excel:export:user', 12, 1, 1, 2, 'embedded');

-- 8. 角色菜单关联（管理员角色拥有所有权限）
-- 先删除已存在的关联，再重新关联
DELETE FROM sys_role_menu WHERE tenant_id = @TENANT_ID AND role_id = @ADMIN_ROLE_ID;
INSERT INTO sys_role_menu (tenant_id, role_id, menu_id) SELECT @TENANT_ID, @ADMIN_ROLE_ID, id FROM sys_menu WHERE tenant_id = @TENANT_ID;

-- 9. 插入系统配置（如果不存在）
INSERT IGNORE INTO sys_config (config_key, config_value, tenant_id) VALUES (
  'login.captcha',
  '{"mode":"none","image":{"keyPrefix":"captcha:image","expireSeconds":120,"width":120,"height":40,"length":4},"slider":{"secondaryEnabled":false,"keyPrefix":"captcha:slider","secondaryKeyPrefix":"captcha:secondary","tokenExpireSeconds":120,"provider":"redis-token"}}',
  @TENANT_ID
);

-- 10. 租户隔离跳过配置（如果不存在）
INSERT IGNORE INTO sys_tenant_ignore(scope, matcher, enabled, remark) VALUES ('TABLE', 'sys_user', 1, '用户表不带租户字段');
INSERT IGNORE INTO sys_tenant_ignore(scope, matcher, enabled, remark) VALUES ('TABLE', 'sys_tenant', 1, '租户表不带租户字段');
INSERT IGNORE INTO sys_tenant_ignore(scope, matcher, enabled, remark) VALUES ('TABLE', 'sys_user_tenant', 1, '用户-租户关联表不带租户字段');

-- 切换到forgex_common库
USE forgex_common;

-- 用户页面样式配置表
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

-- 插入默认样式配置（如果不存在）
INSERT IGNORE INTO sys_user_style_config (user_id, tenant_id, config_key, config_json) VALUES
(@ADMIN_USER_ID, @TENANT_ID, 'layout.style', '{"theme":"default","layout":"side","navTheme":"dark","fixedHeader":true,"autoHideHeader":false,"fixSiderbar":true}');

-- Excel 导入导出配置表
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

-- 插入默认导出/导入配置（租户1）
INSERT IGNORE INTO fx_excel_export_config (tenant_id, table_name, table_code, title, subtitle, export_format, enable_total, version)
VALUES
(@TENANT_ID, '用户导出', 'sys_user', '用户导出', '系统管理-用户管理', 'xlsx', 0, 1),
(@TENANT_ID, '登录日志导出', 'sys_login_log', '登录日志导出', '系统管理-登录日志', 'xlsx', 0, 1);

SET @EXPORT_USER_CFG_ID = (SELECT id FROM fx_excel_export_config WHERE tenant_id=@TENANT_ID AND table_code='sys_user' LIMIT 1);
SET @EXPORT_LOGINLOG_CFG_ID = (SELECT id FROM fx_excel_export_config WHERE tenant_id=@TENANT_ID AND table_code='sys_login_log' LIMIT 1);

INSERT IGNORE INTO fx_excel_export_config_item (tenant_id, config_id, export_field, field_name, order_num)
VALUES
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'id', '用户ID', 1),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'account', '账号', 2),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'username', '用户名', 3),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'phone', '手机号', 4),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'email', '邮箱', 5),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'status', '状态', 6),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'lastLoginTime', '最后登录时间', 7),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'lastLoginIp', '最后登录IP', 8),
(@TENANT_ID, @EXPORT_USER_CFG_ID, 'lastLoginRegion', '最后登录地区', 9);

INSERT IGNORE INTO fx_excel_export_config_item (tenant_id, config_id, export_field, field_name, order_num)
VALUES
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'account', '账号', 1),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'loginIp', '登录IP', 2),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'loginRegion', '归属地', 3),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'userAgent', '浏览器UA', 4),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'loginTime', '登录时间', 5),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'logoutTime', '登出时间', 6),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'logoutReason', '登出原因', 7),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'status', '状态', 8),
(@TENANT_ID, @EXPORT_LOGINLOG_CFG_ID, 'reason', '失败原因', 9);

INSERT IGNORE INTO fx_excel_import_config (tenant_id, table_name, table_code, title, subtitle, version)
VALUES
(@TENANT_ID, '用户导入模板', 'sys_user', '用户导入模板', '系统管理-用户管理', 1);

SET @IMPORT_USER_CFG_ID = (SELECT id FROM fx_excel_import_config WHERE tenant_id=@TENANT_ID AND table_code='sys_user' LIMIT 1);

INSERT IGNORE INTO fx_excel_import_config_item (tenant_id, config_id, import_field, field_type, dict_code, required, order_num)
VALUES
(@TENANT_ID, @IMPORT_USER_CFG_ID, 'account', 'string', NULL, 1, 1),
(@TENANT_ID, @IMPORT_USER_CFG_ID, 'username', 'string', NULL, 1, 2),
(@TENANT_ID, @IMPORT_USER_CFG_ID, 'phone', 'string', NULL, 0, 3),
(@TENANT_ID, @IMPORT_USER_CFG_ID, 'email', 'string', NULL, 0, 4);

-- 切换回forgex_admin库
USE forgex_admin;

-- 创建索引
ALTER TABLE sys_user ADD INDEX idx_username (username);
ALTER TABLE sys_user ADD INDEX idx_account (account);
ALTER TABLE sys_role ADD INDEX idx_role_key (role_key);
ALTER TABLE sys_user_tenant ADD INDEX idx_user_pref (user_id, pref_order);

-- 完成提示
SELECT '数据库初始化完成！' AS message;
SELECT '默认管理员账号：admin' AS username;
SELECT '默认密码：123456' AS password;
